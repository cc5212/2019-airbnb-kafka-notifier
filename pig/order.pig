A = LOAD 'hdfs://cm:9000/uhadoop2019/emiliopablonico/proyecto/calendarFilter2.csv.gz' USING PigStorage(',') AS (listing_id,date:chararray,available,price,adjusted_price,minimum_nights,maximum_nights);
E = FILTER A BY NOT (listing_id IS NULL OR date IS NULL OR available IS NULL OR price IS NULL OR adjusted_price IS NULL OR minimum_nights IS NULL OR maximum_nights IS NULL);
B = FOREACH E GENERATE listing_id,date,available,price,adjusted_price,minimum_nights,maximum_nights,ToDate(date, 'yyyy-MM-dd','America/Los_Angeles') AS daterOrdering;
C = ORDER B by daterOrdering ASC;
D = FOREACH C GENERATE CONCAT(listing_id,',',date,',',available,',',price,',',adjusted_price,',',minimum_nights,',',maximum_nights);
STORE D INTO '/uhadoop2019/emiliopablonico/resultsProyect/r2/';
