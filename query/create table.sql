create table mainManager(
userName Dusername primary key,
password varchar(20)
);

create table bank(
userName Dusername primary key,
password varchar(20),
name Dname UNIQUE,
token token,
id int,
foreign key(id) references wallet(id)
);

create table client(
userName Dusername primary key,
password varchar(20),
name Dname UNIQUE,
id int,
foreign key(name) references bank(name),
foreign key(id) references wallet(id)
);

create table wallet(
id int primary key,
privateKey varchar(20),
publicKey varchar(20)
);
