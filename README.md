# pwned-check

[haveibeenpwned](https://haveibeenpwned.com/Passwords) has downloadable files that contains about 320 million password hashes that have been involved in known data breaches.

This site has a search feature that allows you to check whether a password exists in the list of known breached passwords. From a security perspective, entering passwords into a public website is a very bad idea. Thankfully, the downloadble files make it possible to perform this analysis offline.

Fast random access of a dataset that contains hundreds of millions of records is a great fit for HBase. This project uses Spring-Boot to create a RESTful service that takes password, converts it to a SHA-1 hash, and then checks HBase.

## loading the hashes into HBase

The files contain SHA-1 hashes: one per line. Since HBase is a column store we cannot load rowskeys without at least one column.

We created a Hive table from the raw files:

    CREATE EXTERNAL TABLE password_hashes (
       sha1 STRING
    )
    ROW FORMAT DELIMITED
    LINES TERMINATED BY '\n'
    STORED AS TEXTFILE
    LOCATION 'hdfs://hdp01.woolford.io:8020/data/password-hashes';

Raw text is a pretty inefficient way to store data in Hadoop, so we converted the table to ORC by inserting the text data into an ORC backed Hive table:

    CREATE TABLE password_hashes_orc (
       sha1 STRING,
       hash_exists boolean
    )
    CLUSTERED BY (sha1) INTO 16 BUCKETS
    STORED AS ORC TBLPROPERTIES ("transactional"="true");
    
    INSERT INTO password_hashes_orc SELECT sha1, true FROM password_hashes;

We created the dummy column, so the data could be inserted into HBase:

    UPDATE password_hashes SET hash_exists = true;

The text-formatted table was no longer required, so we replaced it with the ORC version:

    DROP TABLE password_hashes;
    ALTER TABLE password_hashes_orc RENAME TO password_hashes;

We created an HBase-backed Hive table, and inserted the records into HBase:

    CREATE TABLE `password` (
      `sha1` string, 
      `hash_exists` boolean)
    ROW FORMAT SERDE 
      'org.apache.hadoop.hive.hbase.HBaseSerDe' 
    STORED BY 
      'org.apache.hadoop.hive.hbase.HBaseStorageHandler' 
    WITH SERDEPROPERTIES ( 
      'hbase.columns.mapping'=':key,hash_exists:hash_exists', 
      'serialization.format'='1')
    TBLPROPERTIES (
      'hbase.mapred.output.outputtable'='password', 
      'hbase.table.name'='password')

    INSERT INTO password SELECT * FROM password_hashes;

Now that the data is in HBase, we made this accessible via RESTful calls to a Spring Boot application. The HBase properties are read from `src/resources/hbase-site.xml`.

Passwords can be checked by posting to the web services `password-lookup` endpoint. Here's an example using Python:

    $ python
    Python 2.7.12 |Anaconda custom (x86_64)| (default, Jul  2 2016, 17:43:17) 
    >>> import requests
    >>> 
    >>> def lookup_password(password):
    ...     return requests.post('http://localhost:8080/password-lookup', data=password).content
    ... 
    >>> passwords = ["mySecretPassword", "$%37^!IUS@)LL", "biggus dickus"]
    >>> 
    >>> for password in passwords:
    ...     print lookup_password(password)
    ... 
    {"sha1":"F032680299B077AFB95093DE4082F625502B8251","password":"mySecretPassword","hashExists":true}
    {"sha1":"80A7DEA1E43E447A06E596532F69D802A4474764","password":"$%37^!IUS@)LL","hashExists":false}
    {"sha1":"AB35C171990106153BDE747965D44E312482403E","password":"biggus dickus","hashExists":true}

We can see that the rather obvious "mySecretPassword" was found (the SHA-1 hash exists), whereas the string of random characters was not.    
