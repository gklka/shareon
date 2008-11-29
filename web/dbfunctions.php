<?php
// ShareOn P2P server database functions
// (C) Kristof Gruber, 2008

// connect to the database server
function connectDb() {
	
	$dbhost = 'localhost';
	$dbuser = 'mk0804';
	$dbpass = 'Lenuecxm';

	$conn = mysql_connect($dbhost, $dbuser, $dbpass) or die('Error connecting to mysql');

	mysql_query("SET CHARACTER SET UTF8");

	$dbname = 'db_mk0804';
	mysql_select_db($dbname);
	
	return $conn;
}

// control the server
function queryDb($query, $conn) {

	$result = mysql_query($query);
	
	if(!$result) {
		debug("Could not successfully run query ($sql) from DB: " . mysql_error());
		return false;
	}

	if(mysql_num_rows($result) == 0) {
		debug("No rows found, nothing to print so am exiting");
		return false;
	}

	while($row = mysql_fetch_assoc($result)) {
		$ret[] = $row;
	}

	return $ret;
}

// close the database connection
function closeDb($conn) {

	mysql_close($conn);
}


// easy-switchable debug
function debug($string) {
	
	echo($string);
}

?>