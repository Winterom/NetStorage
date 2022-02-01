package appServer.handlersForClientSrv;

import appServer.serviceApp.DBConnection;
import appServer.serviceApp.EntityUser;
import appServer.serviceApp.SrvProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import message.*;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Command> {
    private final EntityUser user;
    private FileMessageHeader fileMessageHeader;
    Path temporaryFileName;

    public MessageHandler(EntityUser user) {
        this.user = user;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        switch (command.getCommandType()) {
            case LIST_FILE_REQUEST:
                listRequest(ctx);
                break;
            case FILE_MESSAGE_HEADER:
                FileMessageHeader fileMessageHeader = (FileMessageHeader) command;
                messageHeader(fileMessageHeader, ctx);
                break;
            case FILE_MESSAGE_PART:
                FileMessagePart part = (FileMessagePart) command;
                fileMessagePartWriter(part, ctx);
            default:
                System.out.println(command.getCommandType());
                break;
        }
    }

    private void fileMessagePartWriter(FileMessagePart part, ChannelHandlerContext ctx) {
        log.info("Получен пакет " + part.getNumberOfPart());
        Path tempPath = SrvProperties.getInstance().
                getPathToRootDir().resolve(temporaryFileName);
        try (FileOutputStream fos = new FileOutputStream(tempPath.toString(), true)) {
            fos.write(part.getBuffer(), 0, part.getCountOfData());
            fos.close();
            //если кусок последний то все закрываем
            //временный файл переименовываем в почти нормальное имя
            if (part.getNumberOfPart() == fileMessageHeader.getQuantityParts()) {
                finalOfSendFile(tempPath, ctx);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            ResultOfSynchFile result = new ResultOfSynchFile(440);
            result.setRelativizePath(fileMessageHeader.getRelativizePath());
            ctx.writeAndFlush(result);
        }
    }

    private void listRequest(ChannelHandlerContext ctx) {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT relpath,lastmodified FROM files where user_id=?;");
            stm.setInt(1, user.getId());
            ResultSet resultSet = stm.executeQuery();
            FileListResponse response = new FileListResponse();
            while (resultSet.next()) {
                LocalDateTime dt = LocalDateTime.parse(resultSet.getString("lastmodified"));
                String path = resultSet.getString("relpath");
                response.getFileList().put(path, dt);
            }
            ctx.writeAndFlush(response);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }


    }

    private void messageHeader(FileMessageHeader fileMessageHeader, ChannelHandlerContext ctx) {
        this.fileMessageHeader = fileMessageHeader;
        Path fileName = Paths.get(fileMessageHeader.getRelativizePath()).getFileName();
        this.temporaryFileName = Paths.get(fileName.toString().
                replaceAll("\\.", "") + ".temporary");
        log.info("получили сообщение с заголовком файла " + fileMessageHeader.getRelativizePath());
        //Если файл с нулевой длиной
        if (fileMessageHeader.getQuantityParts() == 0) {
            try {
                Path tempPath = SrvProperties.getInstance().
                        getPathToRootDir().resolve(temporaryFileName);
                if(!Files.exists(tempPath)){
                    Files.createFile(tempPath);
                }
                finalOfSendFile(tempPath, ctx);
                return;
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
        Path tempPath = SrvProperties.getInstance().getPathToRootDir().resolve(temporaryFileName);
        try {
            if (Files.exists(tempPath)) {
                Files.delete(tempPath);
            }
            Files.createFile(tempPath);//создаем временный файл
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
            ResultOfSynchFile result = new ResultOfSynchFile(405);
            result.setRelativizePath(fileMessageHeader.getRelativizePath());
            ctx.writeAndFlush(result);
        }

    }

    private void finalOfSendFile(Path tempPath, ChannelHandlerContext ctx) throws IOException, SQLException {
        if (!(fileMessageHeader.getCrc32file()== FileUtils.checksumCRC32(tempPath.toFile()))) {
            log.error("Передать то файл передали а контрольная сумма не сходится");
            log.error("Файл результат: " + tempPath);
            ResultOfSynchFile result = new ResultOfSynchFile(400);
            result.setRelativizePath(fileMessageHeader.getRelativizePath());
            Files.delete(tempPath);//удаляем неверно переданный файл
            ctx.writeAndFlush(result);
            return;
        }

        byte[] bytesOfMessage = (fileMessageHeader.getRelativizePath() + user.getLogin()).getBytes(StandardCharsets.UTF_8);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert md != null;
        byte[] theMD5digest = md.digest(bytesOfMessage);
        Path newPath = SrvProperties.getInstance().
                getPathToRootDir().resolve(Path.of(DatatypeConverter.printHexBinary(theMD5digest)) + user.getLogin());
        //Если файл с нулевой длиной то временного файла нет так как мы туда
        // ничего не писали, и кроме заголовка файла на сервер ничего не пришло
        if (!(fileMessageHeader.getQuantityParts() == 0)) {
            if (Files.exists(newPath)) {
                Files.delete(newPath);
            }
            Files.move(tempPath, newPath);
        }
        //Отправляем на сервер что все нормально синхронизировали
        ResultOfSynchFile result = new ResultOfSynchFile(200);
        result.setRelativizePath(fileMessageHeader.getRelativizePath());
        ctx.writeAndFlush(result);

        //Добавляем запись в базу данных о файле
        Connection connection = DBConnection.getInstance().getConnection();
        //Есть ли такая запись в базе данных
        PreparedStatement stmSelect = connection.prepareStatement("SELECT id FROM files where user_id=? and relpath=?");
        stmSelect.setInt(1, user.getId());
        stmSelect.setString(2, fileMessageHeader.getRelativizePath());
        ResultSet rs = stmSelect.executeQuery();
        if (rs.next()) {
            PreparedStatement stmUpdate = connection.prepareStatement("Update files set user_id = ?,relpath = ?,lastmodified = ?,filesize = ?,filename =? where id=?;");
            stmUpdate.setInt(1, user.getId());
            stmUpdate.setString(2, fileMessageHeader.getRelativizePath());
            stmUpdate.setString(3, fileMessageHeader.getLastModified().toString());
            BigDecimal size = BigDecimal.valueOf(fileMessageHeader.getSize());
            stmUpdate.setBigDecimal(4, size);
            stmUpdate.setInt(6, rs.getInt("id"));
            stmUpdate.setString(5, newPath.getFileName().toString());
            stmUpdate.executeUpdate();
        } else {
            PreparedStatement stmInsert = connection.prepareStatement("insert into files (user_id,relpath,lastmodified,filesize,filename) values(?,?,?,?,?);");
            stmInsert.setInt(1, user.getId());
            stmInsert.setString(2, fileMessageHeader.getRelativizePath());
            stmInsert.setString(3, fileMessageHeader.getLastModified().toString());
            BigDecimal size = BigDecimal.valueOf(fileMessageHeader.getSize());
            stmInsert.setBigDecimal(4, size);
            stmInsert.setString(5, newPath.getFileName().toString());
            stmInsert.executeUpdate();
        }
        connection.close();
        fileMessageHeader = null;//обнуляем все для следующего файла
        temporaryFileName = null;
    }


}
