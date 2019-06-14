A = LOAD 'hdfs://cm:9000/uhadoop2019/emiliopablonico/proyecto/calendarUpdate.csv' USING PigStorage('"') AS (tab1, price1, tab3, price2, tab5);
B = FOREACH A GENERATE CONCAT(tab1,REPLACE(REPLACE(price1, '[$]', ''), ',',''),tab3,REPLACE(REPLACE(price2, '[$]', ''), ',',''),tab5);
STORE B INTO '/uhadoop2019/emiliopablonico/resultsProyect/r1/';
