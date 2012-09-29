create table common_region_new select * from ironrhino.common_region;

create table common_region_temp_a select a.id,a.name,a.name as parentName from common_region a where parentId is null;
insert into common_region_temp_a select a.id,a.name,b.name as parentName from common_region a join common_region b on a.parentId=b.id;

create table common_region_temp_b select a.id,a.name,a.name as parentName from common_region_new a where parentId is null;
insert into common_region_temp_b select a.id,a.name,b.name as parentName from common_region_new a join common_region_new b on a.parentId=b.id;

create table common_region_mapping select a.id as oldId,b.id as newId from common_region_temp_a a join common_region_temp_b b on a.name=b.name and substring(a.parentName,1,2)=substring(b.parentName,1,2);

create table common_region_mapping_missing select a.id,a.name,a.parentName from common_region_temp_a a where not exists(select * from common_region_mapping where oldId=a.id);

select * from common_region_mapping_missing;  

--resolve missing mapping from common_region_mapping_missing and insert into common_region_mapping;
--make sure select count(*) from common_region = select count(*) from common_region_mapping

									
update xxx a set a.regionId = (select b.newId from common_region_mapping b where b.oldId=a.regionId);


drop table common_region_temp_a;

drop table common_region_temp_b;

drop table common_region_mapping_missing;

drop table common_region_mapping;

drop table common_region;

rename table common_region_new to common_region;

--rebuild search index;

