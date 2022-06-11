import * as nodemailer from 'nodemailer';
import * as AWS from "aws-sdk";
import {error} from "util";

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
