<?php
// ShareOn P2P server .shareon file downloader
// (C) Kristof Gruber, 2008

include("dbfunctions.php");

// connect the database
$con = connectDb();

$shares = queryDb("SELECT * FROM shares WHERE hash=".$_GET[file], $con);

// set the headers to command browsers to download the file instead of display
header("Content-Type: appliaction/x-shareon");
header("Content-Disposition: attachment; filename=\"".$shares[0][file].".shareon\"");

// file type identifier
echo("SHAREON1\n");

// shared file name
echo($shares[0][file]."\n");

// seeder IPs
foreach($shares as $share) {
	echo($share[ip]."\n");
}

// file end
echo("END\n");

?>
