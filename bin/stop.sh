pid=`cat pid`
if [ -z "$pid" ]; then
  echo Clusterfest is not running.
else
  kill $pid
  echo Clusterfest has stopped.
  rm pid  
fi
