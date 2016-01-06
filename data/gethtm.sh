#!/bin/sh

#학문의 기초
year=$1
semester=$2

if test $# -lt 2; then
  echo "argument error"
  echo "ex) 2012 S"
  exit
fi

#학문의기초 - ALL
filename="htm/"$year"_"$semester"_basics_all.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=10"
echo $filename
wget -q -O $filename $address
#학문의 기초 - 국어와 작문
filename="htm/"$year"_"$semester"_basics_korean.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=10&yungyuk_code=11"
echo $filename
wget -q -O $filename $address
#학문의 기초 - 외국어와 외국문화
filename="htm/"$year"_"$semester"_basics_foreign.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=10&yungyuk_code=12"
echo $filename
wget -q -O $filename $address
#학문의 기초 - 기초과학
filename="htm/"$year"_"$semester"_basics_science.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=10&yungyuk_code=17"
echo $filename
wget -q -O $filename $address

#핵심교양 - 문학과 예술
filename="htm/"$year"_"$semester"_core_art.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=20&yungyuk_code=13"
echo $filename
wget -q -O $filename $address
#핵심교양 - 역사와 철학
filename="htm/"$year"_"$semester"_core_history.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=20&yungyuk_code=14"
echo $filename
wget -q -O $filename $address
#핵심교양 - 사회와 이념
filename="htm/"$year"_"$semester"_core_society.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=20&yungyuk_code=15"
echo $filename
wget -q -O $filename $address
#핵심교양 - 자연의 이해
filename="htm/"$year"_"$semester"_core_nature.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=20&yungyuk_code=16"
echo $filename
wget -q -O $filename $address
#핵심교양 - 자연과 기술
filename="htm/"$year"_"$semester"_core_technology.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=20&yungyuk_code=31"
echo $filename
wget -q -O $filename $address
#핵심교양 - 생명과 환경
filename="htm/"$year"_"$semester"_core_biology.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=20&yungyuk_code=32"
echo $filename
wget -q -O $filename $address

#일반교양 - 국어와 작문
filename="htm/"$year"_"$semester"_normal_korean.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=11"
echo $filename
wget -q -O $filename $address
#일반교양 - 외국어와 외국문화
filename="htm/"$year"_"$semester"_normal_foreign.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=12"
echo $filename
wget -q -O $filename $address
#일반교양 - 문학과 예술
filename="htm/"$year"_"$semester"_normal_art.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=13"
echo $filename
wget -q -O $filename $address
#일반교양 - 역사와 철학
filename="htm/"$year"_"$semester"_normal_history.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=14"
echo $filename
wget -q -O $filename $address
#일반교양 - 사회와 이념
filename="htm/"$year"_"$semester"_normal_society.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=15"
echo $filename
wget -q -O $filename $address
#일반교양 - 자연의 이해
filename="htm/"$year"_"$semester"_normal_nature.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=16"
echo $filename
wget -q -O $filename $address
#일반교양 - 기초과학
filename="htm/"$year"_"$semester"_normal_science.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=17"
echo $filename
wget -q -O $filename $address
#일반교양 - 체육 및 기타
filename="htm/"$year"_"$semester"_normal_exercise.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=18"
echo $filename
wget -q -O $filename $address
#일반교양 - 기초교육 특별프로그램
filename="htm/"$year"_"$semester"_normal_special.htm"
address="https://snuhaksa.snu.ac.kr/ssg/Ssg02102.jsp?job_gubun=1&gaesul_year="$year"&gaesul_hakgi="$semester"&sangwi_yungyuk=30&yungyuk_code=19"
echo $filename
wget -q -O $filename $address
