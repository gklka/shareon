<?php
// ShareOn P2P server
// (C) Kristof Gruber, 2008

// list all active shares
function listShares() {
	$conn = connectDb();
	$ret = queryDb("SELECT file, hash, COUNT(id) as seeders FROM shares GROUP BY hash", $conn);
	
	echo("<table>\n");
	echo("	<tr>\n");
	echo("		<th>File name</td>\n");
	echo("		<th>Seeders</td>\n");
	echo("		<th>Download</th>");
	echo("	</tr>\n");
	foreach($ret as $data) {
		echo("	<tr>\n");
		echo("		<td>".$data[file]."</td>\n");
		echo("		<td><a href=\"index.php?m=seeders&hash=".$data[hash]."\">".$data[seeders]."</a></td>\n");
		echo("		<td><a href=\"download.php?file=".$data[hash]."\">Download</a></td>\n");
		echo("	</tr>\n");
	}
	echo("</table>\n");
	
	closeDb($conn);
}

// list who shares the file
function listSeeders($hash) {
	$conn = connectDb();
	$ret = queryDb("SELECT shares.id, shares.file, shares.ip, count.num
	FROM
		shares,
		(SELECT ip, COUNT(ip) as num FROM shares GROUP BY ip) as count
	WHERE
		shares.hash=".$hash." AND
		shares.ip=count.ip", $conn);

	echo("<h2>".$ret[0][file]."</h2>\n\n");

	echo("<p><a href=\"index.php\">Back</a></p>\n\n");
	
	echo("<p><a href=\"download.php?file=".$hash."\">Download</a></p>\n\n");
	
	echo("<table>\n");
	echo("	<tr>\n");
	echo("		<th>#</td>\n");
	echo("		<th>IP</td>\n");
	echo("		<th>Shared files</td>\n");
	echo("	</tr>\n");
	foreach($ret as $data) {
		echo("	<tr>\n");
		echo("		<td>".$data[id]."</td>\n");
		echo("		<td>".$data[ip]."</td>\n");
		echo("		<td><a href=\"index.php?m=files&ip=".$data[ip]."\">".$data[num]."</a></td>\n");
		echo("	</tr>\n");
	}
	echo("</table>\n");
	
	closeDb($conn);
}

// list all active shares
function listFiles($ip) {
	$conn = connectDb();
	$ret = queryDb("SELECT shares.id, shares.file, shares.hash, count.num
	FROM
		shares,
		(SELECT hash, COUNT(hash) as num FROM shares GROUP BY hash) as count
	WHERE
		shares.ip='".$ip."' AND
		shares.hash=count.hash", $conn);
	
	echo("<h2>".$ip."</h2>\n\n");

	echo("<p><a href=\"index.php\">Back</a></p>\n\n");

	echo("<table>\n");
	echo("	<tr>\n");
	echo("		<th>File name</td>\n");
	echo("		<th>Other seeders</td>\n");
	echo("		<th>Download</th>");
	echo("	</tr>\n");
	foreach($ret as $data) {
		echo("	<tr>\n");
		echo("		<td>".$data[file]."</td>\n");
		echo("		<td><a href=\"index.php?m=seeders&hash=".$data[hash]."\">".($data[num]-1)."</a></td>\n");
		echo("		<td><a href=\"download.php?file=".$data[hash]."\">Download</a></td>\n");
		echo("	</tr>\n");
	}
	echo("</table>\n");
	
	closeDb($conn);
}

?>