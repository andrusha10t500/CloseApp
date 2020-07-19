#!/system/bin/sh
echo $1
index=0
while read line
do
  array[$index]="$line"
  index=$(($index+1))
done < $1

for i in ${array[@]}
  do am force-stop $i
done

