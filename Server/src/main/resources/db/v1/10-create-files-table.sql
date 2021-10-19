create table files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id int not null,
    relpath varchar(260) not null,
    lastmodified varchar(100) not null,
    filesize bigint,
    filename varchar(115),
    FOREIGN KEY (user_id) REFERENCES  users(id) ON DELETE RESTRICT
);