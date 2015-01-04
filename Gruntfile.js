module.exports = function(grunt) {
 
    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        //uglify 설정
        uglify: {
            options: {
                compress: true,
                report: 'gzip',
                banner: '/* <%= grunt.template.today("yyyy-mm-dd") %> */ ' //파일의 맨처음 붙는 banner 설정
            },
            build: {
                src: 'assets/javascripts/result.js', //uglify할 대상 설정
                dest: 'assets/javascripts/result.min.js' //uglify 결과 파일 설정
            }
        },
        //concat 설정
        concat:{
            basic: {
                src: ['assets/javascripts/utils.js', 'assets/javascripts/exportpng.js', 'assets/javascripts/timetable_common.js', 'assets/javascripts/timetable_snutt.js'], //concat 타겟 설정(앞에서부터 순서대로 합쳐진다.)
                dest: 'assets/javascripts/result.js' //concat 결과 파일
            }
        },
        watch:{
            files: ['assets/javascripts/*.js', '!assets/javascripts/result.js', '!assets/javascripts/result.min.js'],
            tasks: ['concat', 'uglify']
        }
    });
 
    // Load the plugin that provides the "uglify", "concat" tasks.
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-watch');

    // Default task(s).
    grunt.registerTask('default', ['concat', 'uglify']); //grunt 명령어로 실행할 작업
 
};