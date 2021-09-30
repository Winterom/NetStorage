create table users (
    id int not null auto_increment,
    login varchar(80) not null,
    isactive tinyint  not null default 1,
    password varchar(50) not null,
                   primary key (id),
                    UNIQUE KEY `login_UNIQUE` (`login`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




