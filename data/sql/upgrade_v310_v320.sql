-- start bug#1405 fix
-- add properties for columns and filters for collectionEvent-s and topSpecimen-s

INSERT INTO entity_property VALUES (32, 'processingEvent.worksheet', 1, 1, 0);

INSERT INTO entity_column VALUES (31, 'Processing Event Worksheet', 32, 0);

INSERT INTO entity_filter VALUES (31, 1, 'Processing Event Worksheet', 32, 0);

-- end bug#1405 fix

-- -----------------------------------------------------------------------
--
-- New security / user management
--
-- -----------------------------------------------------------------------

-- add new objects into the csm database:
insert into csm_protection_element (protection_element_name, object_id, application_id, update_date) values
('edu.ualberta.med.biobank.model.User','edu.ualberta.med.biobank.model.User',2,sysdate()),
('edu.ualberta.med.biobank.model.BbGroup','edu.ualberta.med.biobank.model.BbGroup',2,sysdate()),
('edu.ualberta.med.biobank.model.Principal','edu.ualberta.med.biobank.model.Principal',2,sysdate()),
('edu.ualberta.med.biobank.model.Membership','edu.ualberta.med.biobank.model.Membership',2,sysdate()),
('edu.ualberta.med.biobank.model.Permission','edu.ualberta.med.biobank.model.Permission',2,sysdate()),
('edu.ualberta.med.biobank.model.Role','edu.ualberta.med.biobank.model.Role',2,sysdate()),
('edu.ualberta.med.biobank.model.Comment','edu.ualberta.med.biobank.model.Comment',2,sysdate());

-- add the new object into the protection group with id 1 (the one containing all objects protection elements)
insert into csm_pg_pe (protection_group_id, protection_element_id, update_date)
select 1, protection_element_id, sysdate() from csm_protection_element
where protection_element_name = 'edu.ualberta.med.biobank.model.User'
or protection_element_name = 'edu.ualberta.med.biobank.model.BbGroup'
or protection_element_name = 'edu.ualberta.med.biobank.model.Principal'
or protection_element_name = 'edu.ualberta.med.biobank.model.Membership'
or protection_element_name = 'edu.ualberta.med.biobank.model.Permission'
or protection_element_name = 'edu.ualberta.med.biobank.model.Role'
or protection_element_name = 'edu.ualberta.med.biobank.model.Comment';


-- add new security tables

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE `bb_group` (
  `PRINCIPAL_ID` int(11) NOT NULL,
  `NAME` varchar(255) COLLATE latin1_general_cs DEFAULT NULL,
  `DESCRIPTION` VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
  PRIMARY KEY (`PRINCIPAL_ID`),
  KEY `FK119439A0FF154DAF` (`PRINCIPAL_ID`),
  CONSTRAINT `FK119439A0FF154DAF` FOREIGN KEY (`PRINCIPAL_ID`) REFERENCES `principal` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `group_user` (
  `USER_ID` int(11) NOT NULL,
  `GROUP_ID` int(11) NOT NULL,
  PRIMARY KEY (`GROUP_ID`,`USER_ID`),
  KEY `FK6B1EC1ABB9634A05` (`USER_ID`),
  KEY `FK6B1EC1AB691634EF` (`GROUP_ID`),
  CONSTRAINT `FK6B1EC1AB691634EF` FOREIGN KEY (`GROUP_ID`) REFERENCES `bb_group` (`PRINCIPAL_ID`),
  CONSTRAINT `FK6B1EC1ABB9634A05` FOREIGN KEY (`USER_ID`) REFERENCES `user` (`PRINCIPAL_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `membership` (
  `ID` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  `CENTER_ID` int(11) DEFAULT NULL,
  `STUDY_ID` int(11) DEFAULT NULL,
  `PRINCIPAL_ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uc_membership` (`PRINCIPAL_ID`,`CENTER_ID`,`STUDY_ID`),
  KEY `FKCD0773D6FF154DAF` (`PRINCIPAL_ID`),
  KEY `FKCD0773D6F2A2464F` (`STUDY_ID`),
  KEY `FKCD0773D692FAA705` (`CENTER_ID`),
  CONSTRAINT `FKCD0773D692FAA705` FOREIGN KEY (`CENTER_ID`) REFERENCES `center` (`ID`),
  CONSTRAINT `FKCD0773D6F2A2464F` FOREIGN KEY (`STUDY_ID`) REFERENCES `study` (`ID`),
  CONSTRAINT `FKCD0773D6FF154DAF` FOREIGN KEY (`PRINCIPAL_ID`) REFERENCES `principal` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `membership_permission` (
  `MEMBERSHIP_ID` int(11) NOT NULL,
  `PERMISSION_ID` int(11) NOT NULL,
  PRIMARY KEY (`MEMBERSHIP_ID`,`PERMISSION_ID`),
  KEY `FK1350F1D8F196CF45` (`PERMISSION_ID`),
  KEY `FK1350F1D8D26ABDE5` (`MEMBERSHIP_ID`),
  CONSTRAINT `FK1350F1D8D26ABDE5` FOREIGN KEY (`MEMBERSHIP_ID`) REFERENCES `membership` (`ID`),
  CONSTRAINT `FK1350F1D8F196CF45` FOREIGN KEY (`PERMISSION_ID`) REFERENCES `permission` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `permission` (
  `ID` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  `CLASS_NAME` varchar(255) COLLATE latin1_general_cs NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CLASS_NAME` (`CLASS_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `principal` (
  `ID` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `membership_role` (
  `MEMBERSHIP_ID` int(11) NOT NULL,
  `ROLE_ID` int(11) NOT NULL,
  PRIMARY KEY (`MEMBERSHIP_ID`,`ROLE_ID`),
  KEY `FKEF36B33F14388625` (`ROLE_ID`),
  KEY `FKEF36B33FD26ABDE5` (`MEMBERSHIP_ID`),
  CONSTRAINT `FKEF36B33FD26ABDE5` FOREIGN KEY (`MEMBERSHIP_ID`) REFERENCES `membership` (`ID`),
  CONSTRAINT `FKEF36B33F14388625` FOREIGN KEY (`ROLE_ID`) REFERENCES `role` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `role` (
  `ID` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  `NAME` varchar(255) COLLATE latin1_general_cs NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `role_permission` (
  `ROLE_ID` int(11) NOT NULL,
  `PERMISSION_ID` int(11) NOT NULL,
  PRIMARY KEY (`ROLE_ID`,`PERMISSION_ID`),
  KEY `FK9C6EC93814388625` (`ROLE_ID`),
  KEY `FK9C6EC938F196CF45` (`PERMISSION_ID`),
  CONSTRAINT `FK9C6EC938F196CF45` FOREIGN KEY (`PERMISSION_ID`) REFERENCES `permission` (`ID`),
  CONSTRAINT `FK9C6EC93814388625` FOREIGN KEY (`ROLE_ID`) REFERENCES `role` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `user` (
  `PRINCIPAL_ID` int(11) NOT NULL,
  `LOGIN` varchar(255) COLLATE latin1_general_cs DEFAULT NULL,
  `CSM_USER_ID` bigint(20) DEFAULT NULL,
  `RECV_BULK_EMAILS` bit(1) DEFAULT NULL,
  `FULL_NAME` varchar(255) COLLATE latin1_general_cs DEFAULT NULL,
  `EMAIL` varchar(255) COLLATE latin1_general_cs DEFAULT NULL,
  `NEED_PWD_CHANGE` bit(1) DEFAULT NULL,
  `ACTIVITY_STATUS_ID` int(11) NOT NULL,
  PRIMARY KEY (`PRINCIPAL_ID`),
  KEY `FK27E3CBFF154DAF` (`PRINCIPAL_ID`),
  KEY `FK27E3CBC449A4` (`ACTIVITY_STATUS_ID`),
  CONSTRAINT `FK27E3CBC449A4` FOREIGN KEY (`ACTIVITY_STATUS_ID`) REFERENCES `activity_status` (`ID`),
  CONSTRAINT `FK27E3CBFF154DAF` FOREIGN KEY (`PRINCIPAL_ID`) REFERENCES `principal` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

-- add 'Unknown commenter' which is used in upgrade scripts
insert into principal (id, version) values (1,0);

set @asactive = null;

select id from activity_status where name='Active' into @asactive;

insert into user (principal_id, login, csm_user_id, recv_bulk_emails, full_name, email, need_pwd_change,activity_status_id)
values (1, 'Unknown commenter', -1, 0, 'Unknown commenter', '', 0, @asactive);

-- -----------------------------------------------------------------------
--
-- Global event attributes
--
-- -----------------------------------------------------------------------

ALTER TABLE global_event_attr MODIFY COLUMN LABEL VARCHAR(50) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL;
ALTER TABLE study_event_attr ADD COLUMN GLOBAL_EVENT_ATTR_ID INT(11) NOT NULL COMMENT '', ADD INDEX FK3EACD8EC44556025 (GLOBAL_EVENT_ATTR_ID);

ALTER TABLE study_event_attr
      ADD CONSTRAINT FK3EACD8EC44556025 FOREIGN KEY FK3EACD8EC44556025 (GLOBAL_EVENT_ATTR_ID) REFERENCES global_event_attr (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

UPDATE study_event_attr sea, global_event_attr gea
    SET sea.global_event_attr_id=gea.id
    WHERE sea.label=gea.label;

ALTER TABLE study_event_attr DROP KEY uc_label;
ALTER TABLE study_event_attr DROP FOREIGN KEY FK3EACD8EC5B770B31;
ALTER TABLE study_event_attr DROP INDEX FK3EACD8EC5B770B31;
ALTER TABLE study_event_attr DROP COLUMN LABEL, DROP COLUMN EVENT_ATTR_TYPE_ID;
ALTER TABLE global_event_attr ADD CONSTRAINT LABEL UNIQUE KEY(LABEL);
ALTER TABLE event_attr_type MODIFY COLUMN NAME VARCHAR(50) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL;
ALTER TABLE event_attr_type ADD CONSTRAINT NAME UNIQUE KEY(NAME);

-- -----------------------------------------------------------------------
--
-- Comment field changes
--
-- Needs to run after User and Principal tables are created since
-- it uses the 'Unknown commenter' for comments.
--
-- -----------------------------------------------------------------------

CREATE TABLE comment (
    ID INT(11) NOT NULL auto_increment,
    VERSION INT(11) NOT NULL,
    MESSAGE TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL,
    CREATED_AT    DATETIME NULL DEFAULT NULL,
    USER_ID INT(11) NOT NULL,
    INDEX FK63717A3FB9634A05 (USER_ID),
    PRIMARY KEY (ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE center_comment (
    CENTER_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FKDF3FBC55CDA9FD4F (COMMENT_ID),
    INDEX FKDF3FBC5592FAA705 (CENTER_ID),
    PRIMARY KEY (CENTER_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE collection_event_comment (
    COLLECTION_EVENT_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FK1CFC0199280272F2 (COLLECTION_EVENT_ID),
    INDEX FK1CFC0199CDA9FD4F (COMMENT_ID),
    PRIMARY KEY (COLLECTION_EVENT_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE container_comment (
    CONTAINER_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FK9A6C8C619BFD88CF (CONTAINER_ID),
    INDEX FK9A6C8C61CDA9FD4F (COMMENT_ID),
    PRIMARY KEY (CONTAINER_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE container_type_comment (
    CONTAINER_TYPE_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FK6657C158B3E77A12 (CONTAINER_TYPE_ID),
    INDEX FK6657C158CDA9FD4F (COMMENT_ID),
    PRIMARY KEY (CONTAINER_TYPE_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE dispatch_comment (
    DISPATCH_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FKAFC93B7ACDA9FD4F (COMMENT_ID),
    INDEX FKAFC93B7ADE99CA25 (DISPATCH_ID),
    PRIMARY KEY (DISPATCH_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE dispatch_specimen_comment (
    DISPATCH_SPECIMEN_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FKC3C4FD2DCDA9FD4F (COMMENT_ID),
    INDEX FKC3C4FD2DBCCB06BA (DISPATCH_SPECIMEN_ID),
    PRIMARY KEY (DISPATCH_SPECIMEN_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE origin_info_comment (
    ORIGIN_INFO_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FKFE82842712E55F12 (ORIGIN_INFO_ID),
    INDEX FKFE828427CDA9FD4F (COMMENT_ID),
    PRIMARY KEY (ORIGIN_INFO_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE patient_comment (
    PATIENT_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FK901E2E5B563F38F (PATIENT_ID),
    INDEX FK901E2E5CDA9FD4F (COMMENT_ID),
    PRIMARY KEY (PATIENT_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE processing_event_comment (
    PROCESSING_EVENT_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FKA958114E33126C8 (PROCESSING_EVENT_ID),
    INDEX FKA958114ECDA9FD4F (COMMENT_ID),
    PRIMARY KEY (PROCESSING_EVENT_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE specimen_comment (
    SPECIMEN_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FK73068C08EF199765 (SPECIMEN_ID),
    INDEX FK73068C08CDA9FD4F (COMMENT_ID),
    PRIMARY KEY (SPECIMEN_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE study_comment (
    STUDY_ID INT(11) NOT NULL,
    COMMENT_ID INT(11) NOT NULL,
    CONSTRAINT COMMENT_ID UNIQUE KEY(COMMENT_ID),
    INDEX FKAA027DA9F2A2464F (STUDY_ID),
    INDEX FKAA027DA9CDA9FD4F (COMMENT_ID),
    PRIMARY KEY (STUDY_ID, COMMENT_ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

ALTER TABLE center_comment
      ADD CONSTRAINT FKDF3FBC5592FAA705 FOREIGN KEY (CENTER_ID) REFERENCES center (ID) ON UPDATE NO ACTION ON DELETE NO ACTION,
      ADD CONSTRAINT FKDF3FBC55CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE collection_event_comment ADD CONSTRAINT FK1CFC0199CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FK1CFC0199280272F2 FOREIGN KEY (COLLECTION_EVENT_ID) REFERENCES collection_event (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE comment ADD CONSTRAINT FK63717A3FB9634A05 FOREIGN KEY (USER_ID) REFERENCES user (PRINCIPAL_ID) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE container_comment ADD CONSTRAINT FK9A6C8C61CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FK9A6C8C619BFD88CF FOREIGN KEY (CONTAINER_ID) REFERENCES container (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE container_type_comment ADD CONSTRAINT FK6657C158CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FK6657C158B3E77A12 FOREIGN KEY (CONTAINER_TYPE_ID) REFERENCES container_type (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE dispatch_comment ADD CONSTRAINT FKAFC93B7ADE99CA25 FOREIGN KEY (DISPATCH_ID) REFERENCES dispatch (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FKAFC93B7ACDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE dispatch_specimen_comment ADD CONSTRAINT FKC3C4FD2DBCCB06BA FOREIGN KEY (DISPATCH_SPECIMEN_ID) REFERENCES dispatch_specimen (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FKC3C4FD2DCDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE origin_info_comment ADD CONSTRAINT FKFE828427CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FKFE82842712E55F12 FOREIGN KEY (ORIGIN_INFO_ID) REFERENCES origin_info (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE patient_comment ADD CONSTRAINT FK901E2E5CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FK901E2E5B563F38F FOREIGN KEY (PATIENT_ID) REFERENCES patient (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE processing_event_comment ADD CONSTRAINT FKA958114ECDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FKA958114E33126C8 FOREIGN KEY (PROCESSING_EVENT_ID) REFERENCES processing_event (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE specimen_comment ADD CONSTRAINT FK73068C08CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FK73068C08EF199765 FOREIGN KEY (SPECIMEN_ID) REFERENCES specimen (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE study_comment ADD CONSTRAINT FKAA027DA9CDA9FD4F FOREIGN KEY (COMMENT_ID) REFERENCES comment (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FKAA027DA9F2A2464F FOREIGN KEY (STUDY_ID) REFERENCES study (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- add temp column into comment table to store source id, this id will then be inserted
-- to the corresponding correlation table

ALTER TABLE comment ADD COLUMN SRC_ID INT(11);

-- center comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, convert_tz('1970-01-01 00:00', 'Canada/Mountain', 'GMT'), 1, id
from center where comment is not null and length(comment)>0;

insert into center_comment (center_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- collection_event comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from collection_event where comment is not null and length(comment)>0;

insert collection_event_comment (collection_event_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- container comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from container where comment is not null and length(comment)>0;

insert container_comment (container_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- container_type comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from container_type where comment is not null and length(comment)>0;

insert container_type_comment (container_type_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- dispatch comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from dispatch where comment is not null and length(comment)>0;

insert dispatch_comment (dispatch_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- dispatch_specimen comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from dispatch_specimen where comment is not null and length(comment)>0;

insert dispatch_specimen_comment (dispatch_specimen_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- patient comments -> this column was not in version 3.1.1

-- processing_event comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from processing_event where comment is not null and length(comment)>0;

insert processing_event_comment (processing_event_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- shipment_info comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from shipment_info where comment is not null and length(comment)>0;

insert origin_info_comment (origin_info_id, comment_id)
select oi.id,comment.id from comment
join shipment_info si on si.id=comment.src_id
join origin_info oi on oi.shipment_info_id=si.id
where comment.src_id is not null;

update comment set src_id=null;

-- specimen comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from specimen where comment is not null and length(comment)>0;

insert specimen_comment (specimen_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- study comments

insert into comment (version, message, created_at, user_id, src_id)
select 0, comment, '1970-01-01 00:00', 1, id
from study where comment is not null and length(comment)>0;

insert study_comment (study_id, comment_id)
select src_id,id from comment where src_id is not null;

update comment set src_id=null;

-- clean up

ALTER TABLE center DROP COLUMN COMMENT;
ALTER TABLE collection_event DROP COLUMN COMMENT;
ALTER TABLE container DROP COLUMN COMMENT;
ALTER TABLE container_type DROP COLUMN COMMENT;
ALTER TABLE dispatch DROP COLUMN COMMENT;
ALTER TABLE dispatch_specimen DROP COLUMN COMMENT;
ALTER TABLE processing_event DROP COLUMN COMMENT;
ALTER TABLE shipment_info DROP COLUMN COMMENT;
ALTER TABLE specimen DROP COLUMN COMMENT;
ALTER TABLE study DROP COLUMN COMMENT;

ALTER TABLE comment
      MODIFY COLUMN ID INT(11) NOT NULL,
      DROP COLUMN SRC_ID;


-- -----------------------------------------------------------------------
--
-- Other changes
--
-- -----------------------------------------------------------------------

ALTER TABLE collection_event DROP KEY uc_visit_number;

ALTER TABLE container DROP KEY uc_label, DROP KEY uc_productbarcode;

ALTER TABLE container_type DROP KEY uc_name, DROP KEY uc_nameshort;

ALTER TABLE address
      ADD COLUMN NAME VARCHAR(50) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '';

ALTER TABLE collection_event
      ADD CONSTRAINT uc_ce_visit_number UNIQUE KEY(VISIT_NUMBER, PATIENT_ID);

ALTER TABLE container
      ADD CONSTRAINT uc_c_label UNIQUE KEY(LABEL, CONTAINER_TYPE_ID),
      ADD CONSTRAINT uc_c_productbarcode UNIQUE KEY(PRODUCT_BARCODE, SITE_ID);

ALTER TABLE container_type
      ADD CONSTRAINT uc_ct_nameshort UNIQUE KEY(NAME_SHORT, SITE_ID),
      ADD CONSTRAINT uc_ct_name UNIQUE KEY(NAME, SITE_ID);

ALTER TABLE request DROP FOREIGN KEY FK6C1A7E6FF2A2464F;
ALTER TABLE request DROP INDEX FK6C1A7E6FF2A2464F, DROP COLUMN STUDY_ID;

ALTER TABLE request
      ADD COLUMN RESEARCH_GROUP_ID INT(11) NOT NULL COMMENT '', ADD INDEX FK6C1A7E6F4BD922D8 (RESEARCH_GROUP_ID),
      ADD CONSTRAINT FK6C1A7E6F4BD922D8 FOREIGN KEY FK6C1A7E6F4BD922D8 (RESEARCH_GROUP_ID) REFERENCES center (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE report
      MODIFY COLUMN DESCRIPTION LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE report_filter_value
      MODIFY COLUMN VALUE LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL, MODIFY COLUMN SECOND_VALUE LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;

ALTER TABLE comment MODIFY COLUMN MESSAGE LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE jasper_template MODIFY COLUMN XML LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE log MODIFY COLUMN DETAILS LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE printer_label_template MODIFY COLUMN CONFIG_DATA LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE study_event_attr MODIFY COLUMN PERMISSIBLE LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;

--
-- Changes now that we are using annotation on the model objects
--

-- ABS_ID in container_position is temporary and only used for upgrading the data

CREATE TABLE container_position (
    ID INT(11) NOT NULL auto_increment,
    ABS_ID INT(11) NOT NULL,
    VERSION INT(11) NOT NULL,
    COL INT(11) NOT NULL,
    ROW INT(11) NOT NULL,
    PARENT_CONTAINER_ID INT(11) NOT NULL,
    CONSTRAINT PARENT_CONTAINER_ID UNIQUE KEY(PARENT_CONTAINER_ID, `ROW`, COL),
    INDEX FK39FBB477366CE44 (PARENT_CONTAINER_ID),
    PRIMARY KEY (ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

CREATE TABLE specimen_position (
    ID INT(11) NOT NULL auto_increment,
    VERSION INT(11) NOT NULL,
    COL INT(11) NOT NULL,
    ROW INT(11) NOT NULL,
    POSITION_STRING VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
    CONTAINER_ID INT(11) NOT NULL,
    SPECIMEN_ID INT(11) NOT NULL,
    CONSTRAINT CONTAINER_ID UNIQUE KEY(CONTAINER_ID, `ROW`, COL),
    INDEX FK3E45B080EF199765 (SPECIMEN_ID),
    INDEX FK3E45B0809BFD88CF (CONTAINER_ID),
    CONSTRAINT SPECIMEN_ID UNIQUE KEY(SPECIMEN_ID),
    PRIMARY KEY (ID)
) ENGINE=InnoDB COLLATE=latin1_general_cs;

ALTER TABLE container_position ADD CONSTRAINT FK39FBB477366CE44 FOREIGN KEY (PARENT_CONTAINER_ID) REFERENCES container (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE specimen_position ADD CONSTRAINT FK3E45B0809BFD88CF FOREIGN KEY (CONTAINER_ID) REFERENCES container (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FK3E45B080EF199765 FOREIGN KEY (SPECIMEN_ID) REFERENCES specimen (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE abstract_position DROP FOREIGN KEY FKBC4AE0A69BFD88CF, DROP FOREIGN KEY FKBC4AE0A67366CE44, DROP FOREIGN KEY FKBC4AE0A6EF199765;

INSERT INTO container_position (ABS_ID, VERSION, `ROW`, COL, PARENT_CONTAINER_ID)
       SELECT ID, 0, `ROW`, COL, PARENT_CONTAINER_ID FROM abstract_position
       WHERE discriminator='ContainerPosition';

UPDATE container ct, container_position ctpos
       SET ct.position_id=ctpos.id
       WHERE ct.position_id=ctpos.abs_id;

INSERT INTO specimen_position (VERSION, `ROW`, COL, POSITION_STRING, CONTAINER_ID, SPECIMEN_ID)
       SELECT 0, `ROW`, COL, POSITION_STRING, CONTAINER_ID, SPECIMEN_ID FROM abstract_position
       WHERE discriminator='SpecimenPosition';

ALTER TABLE container
      DROP FOREIGN KEY FK8D995C61AC528270;

ALTER TABLE container
      ADD CONSTRAINT FK8D995C61AC528270
      FOREIGN KEY FK8D995C61AC528270 (POSITION_ID)
      REFERENCES container_position (ID)
      ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE container_position
      MODIFY COLUMN ID INT(11) NOT NULL,
      DROP COLUMN ABS_ID;

ALTER TABLE specimen_position
      MODIFY COLUMN ID INT(11) NOT NULL;

DROP TABLE abstract_position;


ALTER TABLE bb_group DROP FOREIGN KEY FK119439A0FF154DAF;
ALTER TABLE user DROP FOREIGN KEY FK27E3CBC449A4, DROP FOREIGN KEY FK27E3CBFF154DAF;
ALTER TABLE comment DROP FOREIGN KEY FK63717A3FB9634A05;
ALTER TABLE group_user DROP FOREIGN KEY FK6B1EC1AB691634EF, DROP FOREIGN KEY FK6B1EC1ABB9634A05;
ALTER TABLE membership_permission DROP FOREIGN KEY FK1350F1D8D26ABDE5, DROP FOREIGN KEY FK1350F1D8F196CF45;
ALTER TABLE role_permission DROP FOREIGN KEY FK9C6EC938F196CF45, DROP FOREIGN KEY FK9C6EC93814388625;
DROP TABLE bb_group;
DROP TABLE user;
DROP TABLE permission;
ALTER TABLE address DROP COLUMN NAME;
ALTER TABLE collection_event DROP KEY uc_ce_visit_number;
ALTER TABLE container DROP KEY uc_c_label, DROP KEY uc_c_productbarcode;
ALTER TABLE container_type DROP KEY uc_ct_nameshort, DROP KEY uc_ct_name;
ALTER TABLE membership DROP KEY uc_membership;

ALTER TABLE membership_permission
      ADD COLUMN ID INT(11) NOT NULL COMMENT '',
      ADD COLUMN PERMISSION_NAME VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
      ADD INDEX FK1350F1D815E6F8DC (ID);

ALTER TABLE membership_permission
      DROP INDEX FK1350F1D8F196CF45,
      DROP INDEX FK1350F1D8D26ABDE5,
      DROP PRIMARY KEY,
      DROP COLUMN MEMBERSHIP_ID,
      DROP COLUMN PERMISSION_ID;

ALTER TABLE role_permission
      ADD COLUMN ID INT(11) NOT NULL COMMENT '',
      ADD COLUMN PERMISSION_NAME VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
      ADD INDEX FK9C6EC938C226FDBC (ID);

ALTER TABLE role_permission
      DROP INDEX FK9C6EC93814388625,
      DROP INDEX FK9C6EC938F196CF45,
      DROP PRIMARY KEY, DROP COLUMN ROLE_ID,
      DROP COLUMN PERMISSION_ID;

ALTER TABLE center MODIFY COLUMN DISCRIMINATOR VARCHAR(31) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL;
ALTER TABLE container MODIFY COLUMN PATH VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE container_type MODIFY COLUMN NAME VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL, MODIFY COLUMN NAME_SHORT VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE printer_label_template MODIFY COLUMN PRINTER_NAME VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE request MODIFY COLUMN CREATED DATETIME NOT NULL;
ALTER TABLE request_specimen MODIFY COLUMN STATE INT(11) NOT NULL;
ALTER TABLE shipment_info MODIFY COLUMN PACKED_AT DATETIME NOT NULL;
ALTER TABLE specimen MODIFY COLUMN CURRENT_CENTER_ID INT(11) NOT NULL;
ALTER TABLE specimen_type MODIFY COLUMN NAME VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL, MODIFY COLUMN NAME_SHORT VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE study_event_attr MODIFY COLUMN PERMISSIBLE VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE collection_event ADD CONSTRAINT PATIENT_ID UNIQUE KEY(PATIENT_ID, VISIT_NUMBER);
ALTER TABLE container ADD CONSTRAINT SITE_ID_2 UNIQUE KEY(SITE_ID, CONTAINER_TYPE_ID, LABEL), ADD CONSTRAINT SITE_ID UNIQUE KEY(SITE_ID, PRODUCT_BARCODE);
ALTER TABLE container_labeling_scheme ADD CONSTRAINT NAME UNIQUE KEY(NAME);
ALTER TABLE container_type ADD CONSTRAINT SITE_ID_2 UNIQUE KEY(SITE_ID, NAME), ADD CONSTRAINT SITE_ID UNIQUE KEY(SITE_ID, NAME_SHORT);
ALTER TABLE membership ADD CONSTRAINT PRINCIPAL_ID UNIQUE KEY(PRINCIPAL_ID, CENTER_ID, STUDY_ID);

ALTER TABLE comment ADD CONSTRAINT FK63717A3FB9634A05 FOREIGN KEY FK63717A3FB9634A05 (USER_ID) REFERENCES principal (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE group_user ADD CONSTRAINT FK6B1EC1AB691634EF FOREIGN KEY FK6B1EC1AB691634EF (GROUP_ID) REFERENCES principal (ID) ON UPDATE NO ACTION ON DELETE NO ACTION, ADD CONSTRAINT FK6B1EC1ABB9634A05 FOREIGN KEY FK6B1EC1ABB9634A05 (USER_ID) REFERENCES principal (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE membership_permission ADD CONSTRAINT FK1350F1D815E6F8DC FOREIGN KEY FK1350F1D815E6F8DC (ID) REFERENCES membership (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE role_permission ADD CONSTRAINT FK9C6EC938C226FDBC FOREIGN KEY FK9C6EC938C226FDBC (ID) REFERENCES role (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE principal
      ADD COLUMN DISCRIMINATOR VARCHAR(31) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT '',
      ADD COLUMN DESCRIPTION VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
      ADD COLUMN NAME VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
      ADD COLUMN CSM_USER_ID BIGINT(20) NULL DEFAULT NULL COMMENT '',
      ADD COLUMN EMAIL VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
      ADD COLUMN FULL_NAME VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
      ADD COLUMN LOGIN VARCHAR(255) CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL COMMENT '',
      ADD COLUMN NEED_PWD_CHANGE TINYINT(1) NULL DEFAULT NULL COMMENT '',
      ADD COLUMN RECV_BULK_EMAILS TINYINT(1) NULL DEFAULT NULL COMMENT '',
      ADD COLUMN ACTIVITY_STATUS_ID INT(11) NOT NULL COMMENT '',
      ADD CONSTRAINT NAME UNIQUE KEY(NAME),
      ADD CONSTRAINT LOGIN UNIQUE KEY(LOGIN),
      ADD INDEX FK3A16800EC449A4 (ACTIVITY_STATUS_ID);

ALTER TABLE principal
      ADD CONSTRAINT FK3A16800EC449A4 FOREIGN KEY FK3A16800EC449A4 (ACTIVITY_STATUS_ID) REFERENCES activity_status (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- upgrade the unknown commenter user
UPDATE principal
       SET discriminator='User'
       WHERE id=1;

ALTER TABLE comment MODIFY COLUMN MESSAGE TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE jasper_template MODIFY COLUMN XML TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE log MODIFY COLUMN DETAILS TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE printer_label_template MODIFY COLUMN CONFIG_DATA TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE report MODIFY COLUMN DESCRIPTION TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE report_filter_value MODIFY COLUMN VALUE TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL, MODIFY COLUMN SECOND_VALUE TEXT CHARACTER SET latin1 COLLATE latin1_general_cs NULL DEFAULT NULL;
ALTER TABLE printed_ss_inv_item ADD CONSTRAINT TXT UNIQUE KEY(TXT);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

