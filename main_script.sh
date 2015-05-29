#! /usr/bin/env sh

./utils/count.py train.csv > fc.trva.t10.txt

thread_num=10
./converters/parallelizer-b.py -s ${thread_num} ./converters/pre-b.py train.csv train.sp

split -l 6548660 train.sp -d -a 4 tr_
mv tr_0006 va_
cat tr_000* > tr_
rm -rf tr_000*

./converters/subsample.py tr_ tr_sample
./converters/subsample.py va_ va_sample

./converters/change_format.py tr_sample tr_std
./converters/change_format.py va_sample va_std

echo `date`

java -Xmx65g -jar ffm.jar 0.1 0.00002 10 4 true false tr_std va_std > java_result

echo `date`

