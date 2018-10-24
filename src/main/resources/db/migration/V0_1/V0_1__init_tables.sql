CREATE TABLE `JOBS` (
  `JOB_ID`          VARCHAR(255)                  NOT NULL PRIMARY KEY,
  `CONTEXT_NAME`    VARCHAR(255)                  NOT NULL,
  `JAR_ID`          INTEGER                       NOT NULL,
  `CLASSPATH`       VARCHAR(255)                  NOT NULL,
  `START_TIME`      TIMESTAMP                     NOT NULL,
  `END_TIME`        TIMESTAMP                     NULL,
  `ERROR`           TEXT                          NULL
);

CREATE TABLE `execution_jobs` (
  `exec_id` int(11) NOT NULL,
  `project_id` int(11) NOT NULL,
  `version` int(11) NOT NULL,
  `flow_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `job_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `attempt` int(11) NOT NULL,
  `start_time` bigint(20) DEFAULT NULL,
  `end_time` bigint(20) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `input_params` longblob,
  `output_params` longblob,
  `attachments` longblob,
  PRIMARY KEY (`exec_id`,`job_id`,`flow_id`,`attempt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci



CREATE TABLE `COFFEES` (
  `COF_ID` INTEGER NOT NULL AUTO_INCREMENT,
  `COF_NAME` VARCHAR(255) NOT NULL,
  `SUP_ID` INTEGER NOT NULL,
  `PRICE` DECIMAL(9,2) NOT NULL,
  --`SALES`  TINYINT(4) DEFAULT 0,
  `COF_GRADE` TINYINT(4) DEFAULT 0,
  `TOTAL` TINYINT(4) DEFAULT 0,
  PRIMARY KEY(`COF_ID`)
)

