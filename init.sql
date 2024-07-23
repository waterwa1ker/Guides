create table if not exists person (
    id bigint primary key generated by default as identity ,
    first_name varchar,
    last_name varchar,
    description varchar,
    username varchar unique,
    role varchar,
    password varchar,
    referral_link varchar
);

create table if not exists referrals (
    id bigint primary key generated by default as identity,
    referral_owner_id bigint references person(id),
    referral_id bigint references person(id)
);

create table if not exists guide (
    id bigint primary key generated by default as identity,
    main_img varchar,
    description varchar,
    person_id bigint references person(id),
    name varchar,
    price int,
    created_at timestamp default now(),
    count int,
    earnings int
);

create table if not exists chapter (
    id bigint primary key generated by default as identity,
    name varchar,
    text varchar,
    img varchar,
    video varchar,
    guide_id bigint references guide(id)
);

create table if not exists purchased_guides (
    id bigint primary key generated by default as identity,
    person_id bigint references person(id),
    guide_id bigint references guide(id)
);