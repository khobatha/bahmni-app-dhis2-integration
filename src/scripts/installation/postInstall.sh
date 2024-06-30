#!/bin/bash
source /opt/dhis-integration/etc/application.yml
set -x

USER=bahmni
GROUP=bahmni

id -g ${GROUP} 2>/dev/null
if [ $? -eq 1 ]; then
    groupadd ${GROUP}
fi

id ${USER} 2>/dev/null
if [ $? -eq 1 ]; then
    useradd -g ${USER} ${USER}
fi

openmrs_db_url=$(grep -A1 'dhis.password:' /opt/dhis-integration/etc/application.yml | tail -n1); db=${db//*openmrs.db.url: /};
query_string=$(echo "$openmrs_db_url" | grep -o '?[^ ]*');
db_user=$(echo "$query_string" | sed -n 's/.*[?&]user=\([^&]*\).*/\1/p');
db_password=$(echo "$query_string" | sed -n 's/.*[?&]password=\([^&]*\).*/\1/p');


mysql --user="root" --password="P@ssw0rd" --database="openmrs" --execute="CREATE TABLE dhis2_log (
																			id int(6) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
																			report_name varchar(100) NOT NULL,
																			submitted_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
																			submitted_by varchar(30) NOT NULL,
																			report_log varchar(4000) NOT NULL,
																			status varchar(30) NOT NULL,
																			comment varchar(500) DEFAULT NULL,
																			report_month int(11) DEFAULT NULL,
																			report_year int(11) DEFAULT NULL,
																			report_week int(11) DEFAULT NULL,
																			is_weekly_report tinyint(1) NOT NULL DEFAULT 0
																		);"

mysql --user="root" --password="P@ssw0rd" --database="openmrs" --execute="create table dhis2_report_type (id int not null auto_increment, name varchar(255) unique not null, primary key(id));"

mysql --user="root" --password="P@ssw0rd" --database="openmrs" --execute="insert into dhis2_report_type (name)
                                                                          values
																		  ('MRSGeneric'),
																		  ('ERPGeneric'),
																		  ('ELISGeneric');"

mysql --user="root" --password="P@ssw0rd" --database="openmrs" --execute="create table dhis2_schedules (id int not null auto_increment, report_name varchar(255), report_id int, frequency varchar(255), created_by varchar(255), created_date date, target_time datetime,last_run datetime, status varchar(255), enabled boolean, primary key(id), foreign key (report_id) references dhis2_report_type(id),CONSTRAINT unique_reportname_frequency UNIQUE (report_name, frequency));"

mysql --user="root" --password="P@ssw0rd" --database="openmrs" --execute="create table dhis2_pharmacy_periods (id int not null auto_increment, dhis2_schedule_id int , period int, created_by varchar(255), created_date date, start_time datetime, end_time datetime, last_run datetime, status varchar(255), enabled boolean, primary key(id), foreign key (dhis2_schedule_id) references dhis2_schedules(id));"

usermod -s /usr/sbin/nologin bahmni

mkdir -p /opt/dhis-integration/var/log/
mkdir /etc/dhis-integration/
mkdir /var/log/dhis-integration/
mkdir /dhis-integration/
mkdir /dhis-integration-data/
mkdir /var/www/bahmni_config/dhis2/

chown -R bahmni:bahmni /opt/dhis-integration/
chown -R bahmni:bahmni /dhis-integration/
chown -R bahmni:bahmni /dhis-integration-data/
chown -R bahmni:bahmni /var/www/bahmni_config/dhis2/ 
chmod +x /opt/dhis-integration/bin/dhis-integration

mv /opt/dhis-integration/bin/dhis-integration*.jar /opt/dhis-integration/bin/dhis-integration.jar

ln -sf /opt/dhis-integration/etc/application.yml /etc/dhis-integration/dhis-integration.yml
ln -sf /opt/dhis-integration/etc/log4j.properties /etc/dhis-integration/log4j.properties
ln -sf /opt/dhis-integration/var/log/dhis-integration.log /var/log/dhis-integration/dhis-integration.log
ln -sf /opt/dhis-integration/bin/dhis-integration /etc/init.d/dhis-integration



chkconfig --add dhis-integration