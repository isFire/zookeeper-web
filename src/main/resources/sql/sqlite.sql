-- auto-generated definition
create table if not exists zk_data
(
    id      integer
        constraint zk_data_pk
            primary key autoincrement,
    zk_url  text default 'localhost:2181',
    zk_name text
);

insert into zk_data(id, zk_name, zk_url)
values (null, '本地连接', 'localhost:2181');