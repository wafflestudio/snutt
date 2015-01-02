#coding:utf-8

#수강편람을 긁어옴
#인자 : year semester
require 'net/http'
require 'roo'
require 'json'

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

category_map = {} #"xxx.xxx;yyy => "학문의 기초""

#htm file download (wget)
`sh gethtm.sh #{"#{year} #{semester}"}`
Dir["htm/#{year}_#{semester}*.htm"].each do |htmfile|
	result = `ruby parsing_htm.rb #{"#{htmfile}"}`.split("\n")
	#학문의 기초 - ALL
	if htmfile.include?("basics_all") then
		result.each{|x| category_map[x] = "basics" unless category_map[x]}
	#학문의 기초 - 외국어와 외국문화
	elsif htmfile.include?("basics_foreign") then
		result.each{|x| category_map[x] = "basics_foreign"}
	#학문의 기초 - 국어와 작문
	elsif htmfile.include?("basics_korean") then
		result.each{|x| category_map[x] = "basics_korean"}
	#학문의 기초 - 기초과학
	elsif htmfile.include?("basics_science") then
		result.each{|x| category_map[x] = "basics_science"}

	#핵심교양 - 문학과 예술
	elsif htmfile.include?("core_art") then
		result.each{|x| category_map[x] = "core_art"}
	#핵심교양 - 생명과 환경
	elsif htmfile.include?("core_biology") then
		result.each{|x| category_map[x] = "core_biology"}
	#핵심교양 - 역사와 철학
	elsif htmfile.include?("core_history") then
		result.each{|x| category_map[x] = "core_history"}
	#핵심교양 - 자연의 이해
	elsif htmfile.include?("core_nature") then
		result.each{|x| category_map[x] = "core_nature"}
	#핵심교양 - 사회와 이념
	elsif htmfile.include?("core_society") then
		result.each{|x| category_map[x] = "core_society"}
	#핵심교양 - 자연과 기술
	elsif htmfile.include?("core_technology") then
		result.each{|x| category_map[x] = "core_technology"}

	#일반교양 - 문학과 예술
	elsif htmfile.include?("normal_art") then
		result.each{|x| category_map[x] = "normal_art"}
	#일반교양 - 체육 및 기타
	elsif htmfile.include?("normal_exercise") then
		result.each{|x| category_map[x] = "normal_exercise"}
	#일반교양 - 외국어와 외국문화
	elsif htmfile.include?("normal_foreign") then
		result.each{|x| category_map[x] = "normal_foreign"}
	#일반교양 - 역사와 철학
	elsif htmfile.include?("normal_history") then
		result.each{|x| category_map[x] = "normal_history"}
	#일반교양 - 국어와 작문
	elsif htmfile.include?("normal_korean") then
		result.each{|x| category_map[x] = "normal_korean"}
	#일반교양 - 자연의 이해
	elsif htmfile.include?("normal_nature") then
		result.each{|x| category_map[x] = "normal_nature"}
	#일반교양 - 기초과학
	elsif htmfile.include?("normal_science") then
		result.each{|x| category_map[x] = "normal_science"}
	#일반교양 - 사회와 이념
	elsif htmfile.include?("normal_society") then
		result.each{|x| category_map[x] = "normal_society"}
	#일반교양 - 기초교육 특별프로그램
	elsif htmfile.include?("normal_special") then
		result.each{|x| category_map[x] = "normal_special"}
	end
end
puts "category mapping complete"

#download 
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
data = "srchCond=0&pageNo=1&workType=EX&sortKey=&sortOrder=&srchOpenSchyy=#{year}&currSchyy=#{year}&srchOpenShtm=#{shtm}&srchCptnCorsFg=&srchOpenShyr=&srchSbjtCd=&srchSbjtNm=&srchOpenUpSbjtFldCd=&srchOpenSbjtFldCd=&srchOpenUpDeptCd=&srchOpenDeptCd=&srchOpenSubmattFgCd=&srchOpenPntMin=&srchOpenPntMax=&srchCamp=&srchBdNo=&srchProfNm=&srchTlsnAplyCapaCntMin=&srchTlsnAplyCapaCntMax=&srchTlsnRcntMin=&srchTlsnRcntMax=&srchOpenSbjtTmNm=&srchOpenSbjtTm=&srchOpenSbjtTmVal=&srchLsnProgType=&srchMrksGvMthd="
res, data = http.post(path, data)

# open(xls_filename,"w") do |file|
# 	file.print(res.body)
# end
# puts "download complete : #{year}_#{semester}.xls"

#convert
puts "start converting from xls to txt"
excel = Roo::Excel.new(xls_filename);
m = excel.to_matrix

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
		remark = m[i,17].gsub(/
\n/, " ")
		category = category_map["#{course_number};#{lecture_number}"]

=begin
		while true
			snuev_api = nil
			begin
				snuev_api = JSON.parse(Net::HTTP.get(URI.parse(URI.escape("http://snuev.com/lecture/find?code=#{course_number}&professor=#{instructor}"))))
			rescue
				puts "fail"
				snuev_api = nil
			end
			if snuev_api then
				break
			end
		end
		snuev_lec_id = snuev_api["lec_id"]
		snuev_eval_score = (snuev_api["eval_point"].to_f / snuev_api["eval_count"].to_f).round(2)
		puts "(#{i-2}/#{m.row_size-3}) #{course_number} #{instructor} #{course_title} : #{snuev_lec_id} : #{snuev_eval_score}"
=end
		snuev_lec_id = snuev_eval_score = nil

		#classtime 표기 통일
		#수(7,8,9) -> 수(7-3)
		class_time = class_time.split("/").map{|x| convert_classtime(x)}.join("/")

		file.puts "#{classification};#{department};#{academic_year};#{course_number};#{lecture_number};#{course_title};#{credit};#{class_time};#{location};#{instructor};#{quota};#{enrollment};#{remark};#{category};#{snuev_lec_id};#{snuev_eval_score}"
	end
end

File.rename("#{txt_filename}.tmp", txt_filename)

#make data.zip
`sh zip.sh`
`echo '{"updated_at": #{Time.now.to_i}}' > ../../api/sugang.json `
