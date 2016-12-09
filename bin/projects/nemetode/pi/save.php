<?php
$override = $_POST['override'];
$fp = fopen('override.txt','w');
fwrite($fp,$override);
fclose($fp);
?>
