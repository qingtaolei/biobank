ALTER TABLE specimen
      MODIFY COLUMN PLATE_ERRORS VARCHAR(500) CHARACTER SET latin1
             COLLATE latin1_general_cs NULL DEFAULT NULL;
