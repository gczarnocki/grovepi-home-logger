#!/bin/bash

function usage {
	echo "USAGE: ./watcher.sh <folder_path>"
}

loader_path=/home/pi/Desktop/_dev/Loader
loader_name=Loader-1.0-SNAPSHOT.jar
folder="$1"

if [ $# -eq 0 ]; then
	usage
	exit
fi

echo "Watcher Script v1.0"
echo "Folder: ${folder}"

pushd ${folder}

while [ 1 -eq 1 ]; do
	n=`ls -a | egrep *.log.sem | wc -l`

	if [ ${n} -gt 0 ]; then
		for file in $(ls -a | egrep *.log.sem); do
			echo $'\nWykryto plik:' ${file}
			sudo rm ${file} # usunięcie semafora
			data_file=${file%%.sem}
			echo "Uruchomienie programu Loader..."
			echo "Plik:" ${data_file}
			java -jar ${loader_path}/${loader_name} ${data_file}
		done
	else 
		sleep 5
		echo -n .
	fi
done

popd

exit 0
