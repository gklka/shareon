<?php
// ShareOn P2P server
// (C) Kristof Gruber, 2008

include("dbfunctions.php");
include("config.php");
include("functions.php");

// print the html header
header("Content-type: text/html; charset=utf-8");
include("header.php");

switch($_GET[m]) {
	case "seeders":
		listSeeders($_GET["hash"]);
		break;
	case "files":
		listFiles($_GET["ip"]);
		break;
	default:
		listShares();
		break;
}

// print the html footer
include("footer.php");

?>