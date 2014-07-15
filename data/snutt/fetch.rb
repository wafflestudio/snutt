#coding:utf-8

#수강편람을 긁어옴
#인자 : year semester

require 'net/http'
require 'roo'
require 'json'
require 'fileutils'

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

#교과구분 [srchOpenSubmattFgCd, srchOpenSbjtFldCd, (자작)영문코드, 한국이름]
fields = [
["A", "40", "foundation_thinking", "학문의 기초: 사고와 표현"],
["A", "41", "foundation_foreign", "학문의 기초: 외국어"],
["A", "42", "foundation_math", "학문의 기초: 수량적 분석과 추론"],
["A", "43", "foundation_science", "학문의 기초: 과학적 사고와 실험"],
["A", "44", "foundation_computer", "학문의 기초: 컴퓨터와 정보 활용"],
["A", "45", "world_literature", "학문의 세계: 언어와 문학"],
["A", "46", "world_culture", "학문의 세계: 문화와 예술"],
["A", "47", "world_history", "학문의 세계: 역사와 철학"],
["A", "48", "world_politics", "학문의 세계: 정치와 경제"],
["A", "49", "world_human", "학문의 세계: 인간과 사회"],
["A", "50", "world_nature", "학문의 세계: 자연과 기술"],
["A", "51", "world_life", "학문의 세계: 생명과 환경"],
["A", "52", "elective_physical", "선택교양: 체육"],
["A", "53", "elective_art", "선택교양: 예술 실기"],
["A", "54", "elective_leadership", "선택교양: 대학과 리더십"],
["A", "55", "elective_creativity", "선택교양: 창의와 융합"],
["A", "56", "elective_korea", "선택교양: 한국의 이해"],
["B", "", "requisite", "전필"],
["C", "", "elective", "전선"],
["D", "", "general", "일선"],
["E", "", "teaching", "교직"],
["F", "", "research", "논문"],
["G", "", "graduate", "대학원"]
]

#download 
puts "Start fetching...#{year}/#{semester}"

FileUtils.mkdir_p "xls/#{year}_#{semester}"
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

fields.each do |field|
    category = field[0]
    fieldCode = field[1]
    fieldNameEng = field[2]

    data = "srchCond=1&pageNo=1&workType=EX&sortKey=&sortOrder=&srchOpenSchyy=#{year}&currSchyy=#{year}&srchOpenShtm=#{shtm}&srchCptnCorsFg=&srchOpenShyr=&srchSbjtCd=&srchSbjtNm=&srchOpenUpSbjtFldCd=&srchOpenSbjtFldCd=#{fieldCode}&srchOpenUpDeptCd=&srchOpenDeptCd=&srchOpenSubmattFgCd=#{category}&srchOpenPntMin=&srchOpenPntMax=&srchCamp=&srchBdNo=&srchProfNm=&srchTlsnAplyCapaCntMin=&srchTlsnAplyCapaCntMax=&srchTlsnRcntMin=&srchTlsnRcntMax=&srchOpenSbjtTmNm=&srchOpenSbjtTm=&srchOpenSbjtTmVal=&srchLsnProgType=&srchMrksGvMthd="
    res, data = http.post(path, data)
    
    xls_filename="#{Dir.getwd()}/xls/#{year}_#{semester}/#{year}_#{semester}_#{fieldNameEng}.xls"
    open(xls_filename,"w") do |file|
    file.print(res.body)
        puts "download complete : #{year}_#{semester}_#{fieldNameEng}.xls"
    end
end
puts "download complete, start converting"
#convert
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

open("#{txt_filename}.tmp", "w") do |file|
    file.puts "#{year}/#{semester}"
    file.puts Time.now.localtime().strftime("%Y-%m-%d %H:%M:%S")
    file.puts "classification;department;academic_year;course_number;lecture_number;course_title;credit;class_time;location;instructor;quota;enrollment;remark;category;snuev_lec_id;snuev_eval_score"
    
    fields.each do |field|
        fieldNameEng = field[2]
        xls_filename="#{Dir.getwd()}/xls/#{year}_#{semester}/#{year}_#{semester}_#{fieldNameEng}.xls"
        begin
            excel = Roo::Excel.new(xls_filename)
            m = excel.to_matrix

            3.upto(m.row_size-1) do |i|
                classification = m[i,0]
                department = m[i,1]
                academic_year = m[i,2]
                if academic_year == "학사"
                    academic_year = m[i,3]
                end
                    course_number = m[i,4]
                    lecture_number = m[i,5]
                    course_title = m[i,6]
                if m[i,7].to_s.length > 1
                    course_title = course_title + "(#{m[i,7]})"
                end
                course_title = course_title.gsub(/;/, ':')
                credit = m[i,8].to_i
                class_time = m[i,11]
                location = m[i,13]
                instructor = m[i,14]
                quota = m[i,15].to_i
                enrollment = m[i,16].to_i
                remark = m[i,17].gsub(/\n/, " ")
                category = fieldNameEng
                snuev_lec_id = snuev_eval_score = nil
            #classtime 표기 통일
            #수(7,8,9) -> 수(7-3)
                class_time = class_time.split("/").map{|x| convert_classtime(x)}.join("/")

                file.puts "#{classification};#{department};#{academic_year};#{course_number};#{lecture_number};#{course_title};#{credit};#{class_time};#{location};#{instructor};#{quota};#{enrollment};#{remark};#{category};#{snuev_lec_id};#{snuev_eval_score}"
            end
            puts "converted_#{fieldNameEng}"
        rescue
            puts "empty_#{fieldNameEng}"
        end     
    end
end
puts "------converting complete--------"

File.rename("#{txt_filename}.tmp", txt_filename)

#make data.zip
`sh zip.sh`
`echo '{"updated_at": #{Time.now.to_i}}' > ../../api/sugang.json `