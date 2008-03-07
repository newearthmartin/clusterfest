pid=`cat pid`
if [ -z "$pid" ]; then
  echo Clusterfest webapp is not running.
else
  kill $pid
  echo Clusterfest webapp has stopped.
  rm pid  
fi
