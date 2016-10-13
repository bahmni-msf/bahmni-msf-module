SET @concept_source_id = 0;
SET @uuid = 0;
SET @time_now = 0;
SET @concept_reference_term_id = 0;
SET @concept_map_type_id = 0;
SET @concept_id = 0;
SELECT concept_source_id INTO @concept_source_id FROM concept_reference_source WHERE name = "Abbreviation";
SELECT concept_map_type_id INTO @concept_map_type_id FROM concept_map_type WHERE name='SAME-AS';

SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('EG', 'EG', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='EG';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Egyptian' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('IQ', 'IQ', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='IQ';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Iraqi' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('JO', 'JO', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='JO';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Jordanian' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('LB', 'LB', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='LB';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Lebanese' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('LY', 'LY', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='LY';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Libyan' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('PS', 'PS', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='PS';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Palestinian' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('SA', 'SA', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='SA';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Saudi Arabian' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('SY', 'SY', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='SY';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Syrian' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);


SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('YE', 'YE', 1, @time_now, @concept_source_id, 0, @uuid);
SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='YE';
SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Yemeni' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);
