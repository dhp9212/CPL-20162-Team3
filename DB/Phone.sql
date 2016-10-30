drop database if exists phone;
create database phone;
use phone;

create table phoneBasic(
	pid int(10),
    pNumber char(30),
    primary key(pid)
    );
    
create table State(
    Sid int(30),
    pIPadress char(30),
	IMEI char(30),
    foreign key(Sid) references phoneBasic(pid)
	);
    
create table HwInfo(
	Hwid int(10),
	ModelNumber char(30),
    OriginalNumber int,
    SN char(30),
	pWiFiMAC char(100),
    pBluetooth char(100),
    foreign key(Hwid) references phoneBasic(pid)
    );
    
create table SwInfo(		
	Swid int(10),
	BuildNumber char(30),
    SoftwareVersion char(30),
    foreign key(Swid) references phoneBasic(pid)
    );
    
insert into phoneBasic values(1,'010-3494-8235');
insert into State values(1,'199.0.0.4', '35714407359844');
insert into HwInfo values(1, 'LG-F700S', 039444, '604KPED0039444','AC:0D;1B:F2:EC:58',0000000);
insert into SwInfo values(1,'MMB29M','F700S11a');

select * from phoneBasic;
select * from State;
select * from HwInfo;
select * from SwInfo;