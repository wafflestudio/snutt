#coding:utf-8

#수강편람을 긁어옴
#인자 : year semester
require 'net/http'
require 'roo'
require 'json'
require 'roo-xls'

if ARGV.length != 2 then
  puts "Argument error !"
  puts "usage example : ruby fetch.rb 2012 S"
  exit!
end
year = ARGV[0]
semester = ARGV[1] #1/S/2/W

if !(year.to_i > 2000) then
  puts "First argument should be year"
  exit!
elsif !["1", "2", "S", "W"].include?(semester) then
  puts "Second argument should be in [1, 2, S, W]"
  exit!
end

general_types_to_code =
{
# 학문의 기초
"foundation_writing"=>40,   #사고와 표현
"foundation_language"=>41,  #외국어
"foundation_math"=>42,      #수량적 분석과 추론
"foundation_science"=>43,   #과학적 사고와 실험
"foundation_computer"=>44,  #컴퓨터와 정보 활용
# 학문의 세계
"knowledge_literature"=>45, #언어와 문학
"knowledge_art"=>46,        #문화와 예술
"knowledge_history"=>47,    #역사와 철학
"knowledge_politics"=>48,   #정치와 경제
"knowledge_human"=>49,      #인간과 사회
"knowledge_nature"=>50,     #자연과 기술
"knowledge_life"=>51,       #생명과 환경
# 선택교양
"general_physical"=>52,     #체육
"general_art"=>53,          #예술실기
"general_college"=>54,      #대학과 리더십
"general_creativity"=>55,   #창의와 융합
"general_korean"=>56        #한국의 이해
}



#download whole coursebook
puts "Start fetching...#{year}/#{semester}"

xls_filename="#{Dir.getwd()}/xls/#{year}_#{semester}.xls"
txt_filename="#{Dir.getwd()}/txt/#{year}_#{semester}.txt"

http = Net::HTTP.new('sugang.snu.ac.kr', 80)
path="/sugang/cc/cc100excel.action"
case semester
when '1'
  shtm = 'U000200001U000300001'
when '2'
  shtm = 'U000200002U000300001'
when 'S'
  shtm = 'U000200001U000300002'
when 'W'
  shtm = 'U000200002U000300002'
end
data = "srchCond=1&pageNo=1&workType=EX&sortKey=&sortOrder=&srchOpenSchyy=#{year}&currSchyy=#{year}&srchOpenShtm=#{shtm}&srchCptnCorsFg=&srchOpenShyr=&srchSbjtCd=&srchSbjtNm=&srchOpenUpSbjtFldCd=&srchOpenSbjtFldCd=&srchOpenUpDeptCd=&srchOpenDeptCd=&srchOpenMjCd=&srchOpenSubmattFgCd=&srchOpenPntMin=&srchOpenPntMax=&srchCamp=&srchBdNo=&srchProfNm=&srchTlsnAplyCapaCntMin=&srchTlsnAplyCapaCntMax=&srchTlsnRcntMin=&srchTlsnRcntMax=&srchOpenSbjtTmNm=&srchOpenSbjtTm=&srchOpenSbjtTmVal=&srchLsnProgType=&srchMrksGvMthd="
res, data = http.post(path, data)

open(xls_filename,"w") do |file|
  file.print(res.body)
end
puts "download complete : #{year}_#{semester}.xls"

#download coursebook for each type of general courses
general_types_to_code.keys.each do |type|
  xls_filename_typed="#{Dir.getwd()}/xls/#{year}_#{semester}_#{type}.xls"
  data = "srchCond=1&pageNo=1&workType=EX&sortKey=&sortOrder=&srchOpenSchyy=#{year}&currSchyy=#{year}&srchOpenShtm=#{shtm}&srchCptnCorsFg=&srchOpenShyr=&srchSbjtCd=&srchSbjtNm=&srchOpenUpSbjtFldCd=&srchOpenSbjtFldCd=#{general_types_to_code[type]}&srchOpenUpDeptCd=&srchOpenDeptCd=&srchOpenMjCd=&srchOpenSubmattFgCd=&srchOpenPntMin=&srchOpenPntMax=&srchCamp=&srchBdNo=&srchProfNm=&srchTlsnAplyCapaCntMin=&srchTlsnAplyCapaCntMax=&srchTlsnRcntMin=&srchTlsnRcntMax=&srchOpenSbjtTmNm=&srchOpenSbjtTm=&srchOpenSbjtTmVal=&srchLsnProgType=&srchMrksGvMthd="
  
  res, data = http.post(path, data)
  open(xls_filename_typed,"w") do |file|
    file.print(res.body)
  end
  puts "download complete : #{year}_#{semester}_#{type}.xls"
end


def convert_classtime(time)
  wday = time[0]
  begin
    times = time.split("(")[1].split(")")[0].split("~")
  rescue
    return ""
  end
  from = times[0]
  from_hour = from.split(":")[0].to_i
  from_min  = from.split(":")[1].to_i
  to = times[1]
  to_hour = to.split(":")[0].to_i
  to_min  = to.split(":")[1].to_i

  from_ctime = (from_hour-8)
  from_ctime = from_ctime + 0.5 if from_min == 30
  to_ctime = (to_hour-8)
  to_ctime = to_ctime + 0.5 if to_min == 20 or to_min == 15
  to_ctime = to_ctime + 1   if to_min == 50 or to_min == 45
  duration = to_ctime - from_ctime


  return "#{wday}(#{from_ctime}-#{duration})"
end

# Converts i-th row of m into string. 
# Returns "" for 교양 courses, if type is not given.
def row_to_string(m, i, type="")
  classification = m[i,0]
  if classification == "교양" && type==""
    return ""
  end
  department = m[i,2]
  academic_year = m[i,3]
  if academic_year == "학사"
    academic_year = m[i,4]
  end
  course_number = m[i,5]
  lecture_number = m[i,6]
  course_title = m[i,7]
  if m[i,8].to_s.length > 1
    course_title = course_title + "(#{m[i,8]})"
  end
  course_title = course_title.gsub(/;/, ':')
  credit = m[i,9].to_i
  class_time = m[i,12]
  location = m[i,14]
  instructor = m[i,15]
  quota = m[i,16].split(" ")[0].to_i
  enrollment = m[i,17].to_i
  remark = m[i,18].gsub(/
\n/, " ")
  category = type;
  snuev_lec_id = snuev_eval_score = nil

  #classtime 표기 통일
  #수(7,8,9) -> 수(7-3)
  class_time = class_time.split("/").map{|x| convert_classtime(x)}.join("/")

  return "#{classification};#{department};#{academic_year};#{course_number};#{lecture_number};#{course_title};#{credit};#{class_time};#{location};#{instructor};#{quota};#{enrollment};#{remark};#{category};#{snuev_lec_id};#{snuev_eval_score}"
end


#convert
puts "start converting from xls to txt"
excel = Roo::Excel.new(xls_filename);
m = excel.to_matrix

open("#{txt_filename}.tmp", "w") do |file|
  file.puts "#{year}/#{semester}"
  file.puts Time.now.localtime().strftime("%Y-%m-%d %H:%M:%S")
  file.puts "classification;department;academic_year;course_number;lecture_number;course_title;credit;class_time;location;instructor;quota;enrollment;remark;category;snuev_lec_id;snuev_eval_score"
  3.upto(m.row_size-1) do |i|
    str = row_to_string(m, i)
    if str==""
      next
    end
    file.puts str
  end

  puts "start putting general courses"
  general_types_to_code.keys.each do |type|
    xls_filename_typed="#{Dir.getwd()}/xls/#{year}_#{semester}_#{type}.xls"
    # during summer/winter session, some type of general courses are not open
    begin
      excel = Roo::Excel.new(xls_filename_typed);
      m = excel.to_matrix
    rescue RuntimeError 
      next
    end
    puts "putting #{type}"
    3.upto(m.row_size-1) do |i|
      str = row_to_string(m, i, type)
      file.puts str
    end
  end

end

File.rename("#{txt_filename}.tmp", txt_filename)

#make data.zip
`sh zip.sh`
`echo '{"updated_at": #{Time.now.to_i}}' > ../../api/sugang.json `
