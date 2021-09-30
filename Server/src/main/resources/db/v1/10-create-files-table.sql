create table files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id int not null,
    fullpath varchar(400) not null,
    lastmodified datetime not null,
    filesize bigint,
    FOREIGN KEY (user_id) REFERENCES  users(id) ON DELETE RESTRICT
);