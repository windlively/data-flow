create schema if not exists common;
create schema if not exists weibo;

create table common.news
(
    id           varchar(128) not null,
    title        varchar(255) null,
    content      text         null,
    send_account varchar(255) null,
    create_time  datetime     null,
    update_time  datetime     null,
    constraint news_id_uindex
        unique (id)
);

alter table common.news
    add primary key (id);

create table weibo.user
(
    id              varchar(20)  not null
        primary key,
    nickname        varchar(30)  null,
    gender          varchar(10)  null,
    location        varchar(200) null,
    birthday        varchar(40)  null,
    description     varchar(400) null,
    verified_reason varchar(140) null,
    talent          varchar(200) null,
    education       varchar(200) null,
    work            varchar(200) null,
    weibo_num       int          null,
    following       int          null,
    followers       int          null
)
    charset = utf8mb4;

create table weibo.weibo
(
    id                varchar(10)          not null
        primary key,
    user_id           varchar(12)          null,
    content           varchar(5000)        null,
    article_url       varchar(200)         null,
    original_pictures varchar(3000)        null,
    retweet_pictures  varchar(3000)        null,
    original          tinyint(1) default 1 not null,
    video_url         varchar(300)         null,
    publish_place     varchar(100)         null,
    publish_time      datetime             not null,
    publish_tool      varchar(30)          null,
    up_num            int                  not null,
    retweet_num       int                  not null,
    comment_num       int                  not null
)
    charset = utf8mb4;

create table weibo.weibo_spider_result
(
    id                varchar(128)  not null
        primary key,
    user_id           bigint        null,
    content           text          null,
    article_url       varchar(1024) null,
    original_pictures varchar(1024) null,
    column_6          int           null,
    video_url         varchar(1024) null,
    publish_place     varchar(255)  null,
    publish_time      datetime      null,
    publish_tool      varchar(255)  null,
    up_num            int           null,
    retweet_num       int           null,
    comment_num       int           null
);

create table common.weibo_user
(
    id              varchar(20)  not null
        primary key,
    nickname        varchar(30)  null,
    gender          varchar(10)  null,
    location        varchar(200) null,
    birthday        varchar(40)  null,
    description     varchar(400) null,
    verified_reason varchar(140) null,
    talent          varchar(200) null,
    education       varchar(200) null,
    work            varchar(200) null,
    weibo_num       int          null,
    following       int          null,
    followers       int          null
);



