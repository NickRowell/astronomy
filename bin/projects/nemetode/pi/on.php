<?php
exec('/usr/local/bin/gpio -g write 10 0; sleep 1; /usr/local/bin/gpio -g write 10 1');
?>
