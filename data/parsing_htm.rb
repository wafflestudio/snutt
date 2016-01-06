#!/usr/bin/env ruby

if ARGV.length != 1 then
  puts "Argument error (filename needed)"
  exit!
end

filename=ARGV[0]
file = File.new(filename, "r")
cnt = 0
result = []
tmp = []
while (line = file.gets)
  if line.include?("pstyle2")
    if cnt == 1 then
      #course number
      tmp.push(line.gsub(/<td class="pstyle2">/,"").gsub(/<\/td>/,"").gsub(/&nbsp;/,"").gsub(/\n/,""))
    elsif cnt == 2 then
      #lecture number
      tmp.push(line.gsub(/<td class="pstyle2">/,"").gsub(/<\/td>/,"").gsub(/&nbsp;/,"").gsub(/\n/,""))
    elsif cnt == 3 then
      result.push(tmp.join(';'))
      tmp = []
    end
    cnt = (cnt + 1) % 12
  end
end

puts result
