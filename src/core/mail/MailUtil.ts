import * as nodemailer from 'nodemailer';
import * as AWS from "aws-sdk";
import {error} from "util";

// AWS.config.loadFromPath(__dirname + '/aws_config.json');
AWS.config.update({
  region: 'ap-northeast-2'
});

const transporter = nodemailer.createTransport({
  SES: new AWS.SES({
    apiVersion: '2010-12-01'
  })
})

export function sendMail(to: String, subject: String, body: String) {
  transporter.sendMail({
    from: 'snutt@wafflestudio.com',
    to: to,
    subject: subject,
    html: body
  }, (err, info) => {
    if (err) {
      error(err);
    }
  });
}

export function sendVerificationCodeMail(to: String, code: String) {
  const body =
    `<h2>인증번호 안내</h2><br/>` +
    `안녕하세요. SNUTT입니다. <br/> ` +
    `<b>아래의 인증번호 6자리를 진행 중인 화면에 입력하여 3분내에 인증을 완료해주세요.</b><br/><br/>` +
    `<h3>인증번호</h3><h3>${code}</h3><br/><br/>` +
    `인증번호는 이메일 발송 시점으로부터 3분 동안 유효합니다.`;
  sendMail(to, `[SNUTT] 인증번호 [${code}] 를 입력해주세요`, body);
}
