/**
 * test/api/user_test.js
 * These tests are for routes/api/user.js
 * supertest: https://github.com/visionmedia/supertest
 * mocha: http://mochajs.org/#usage
 */
import sinon = require('sinon');
import assert = require('assert');
import supertest = require('supertest');

import FacebookService = require('@app/core/facebook/FacebookService');
import InvalidFbIdOrTokenError from '@app/core/facebook/error/InvalidFbIdOrTokenError';
import ErrorCode from '@app/api/enum/ErrorCode';

export = function(request: supertest.SuperTest<supertest.Test>) {
  var token;
  var token2;
  var token_temp;

  let sinonSandbox = sinon.createSandbox();

  afterEach(function() {
    sinonSandbox.restore();
  });

  it('Log-in succeeds', function(done) {
    request.post('/auth/login_local')
      .send({id:"snutt", password:"abc1234"})
      .expect(200)
      .end(function(err, res){
        token = res.body.token;
        done(err);
      });
  });

  it('Token transaction works', function(done) {
    request.get('/user/info')
      .set('x-access-token', token)
      .expect(200)
      .end(function(err, res){
        done(err);
      });
  });

  it('Token transaction fails when no token', function(done) {
    request.get('/user/info')
      .expect(401)
      .end(function(err, res){
        assert.equal(res.body.errcode, ErrorCode.NO_USER_TOKEN);
        done(err);
      });
  });

  it('Token transaction fails when incorrect token', function(done) {
    request.get('/user/info')
      .set('x-access-token', "abcd")
      .expect(403)
      .end(function(err, res){
        assert.equal(res.body.errcode, ErrorCode.WRONG_USER_TOKEN);
        done(err);
      });
  });

  describe('Log-in fails when', function() {
    it('user does not exist', function(done) {
      request.post('/auth/login_local')
        .send({id:"FakeSnutt", password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_ID);
          done(err);
        });
    });
    it('wrong password', function(done) {
      request.post('/auth/login_local')
        .send({id:"snutt", password:"abc12345"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_PASSWORD);
          done(err);
        });
    });
  });

  it('Register succeeds', function(done) {
    request.post('/auth/register_local')
      .send({id:"snutt2", password:"abc1234f", email:"abcd@snutt.com"})
      .expect(200)
      .end(function(err, res){
        assert.equal(res.body.message, 'ok');
        done(err);
      });
  });

  it('Log-in registered account', function(done) {
    request.post('/auth/login_local')
      .send({id:"snutt2", password:"abc1234f"})
      .expect(200)
      .end(function(err, res){
        token2 = res.body.token;
        done(err);
      });
  });

  it('Get user info', function(done) {
    request.get('/user/info')
      .set('x-access-token', token2)
      .expect(200)
      .end(function(err, res) {
        assert.equal(res.body.email, "abcd@snutt.com");
        done(err);
      })
  })

  describe('password change', function(){
    it('succeeds', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"abc1234*", old_password:"abc1234f"})
        .expect(200)
        .end(function(err, res){
          token2 = res.body.token;
          done(err);
        });
    });

    it('fails when no old password', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_PASSWORD);
          done(err);
        });
    });

    it('fails when wrong old password', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"abc1234!"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_PASSWORD);
          done(err);
        });
    });

    it('fails when no password', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({old_password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('fails when password too short', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"a1111", old_password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('fails when password too long', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"abcdefghijklmnopqrst1", old_password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('fails when no password only digits', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"111111", old_password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('fails when no password only letters', function(done) {
     request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"abcdef", old_password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('fails when no password with whitespace', function(done) {
      request.put('/user/password')
        .set('x-access-token', token2)
        .send({new_password:"sql injection", old_password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });
  });

  it('Auto-generated default timetable', function(done) {
    request.get('/tables/')
      .set('x-access-token', token2)
      .expect(200)
      .end(function(err, res) {
        if (err) done(err);
        assert.equal(res.body[0].title, "나의 시간표");
        assert.equal(res.body[0].year, 2016);
        assert.equal(res.body[0].semester, 3);
        done();
      });
  });

  describe('Register fails when', function() {
    it('No ID', function(done) {
      request.post('/auth/register_local')
        .send({password:"IDontNeedID"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_ID);
          done(err);
        });
    });

    it('Duplicate ID', function(done) {
      request.post('/auth/register_local')
        .send({id:"snutt", password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.DUPLICATE_ID);
          done(err);
        });
    });

    it('Weird ID', function(done) {
      request.post('/auth/register_local')
        .send({id:"snutt##*", password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_ID);
          done(err);
        });
    });

    it('Too short ID', function(done) {
      request.post('/auth/register_local')
        .send({id:"tt", password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_ID);
          done(err);
        });
    });

    it('Too long ID', function(done) {
      request.post('/auth/register_local')
        .send({id:"ThisIsVeryLongIdYouKnowThatThisIsFreakingLongManVeryLong", password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_ID);
          done(err);
        });
    });

    it('No password', function(done) {
      request.post('/auth/register_local')
        .send({id:"IDontNeedPw"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('Password too short', function(done) {
      request.post('/auth/register_local')
        .send({id:"idiot", password:"a1111"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('Password too long', function(done) {
      request.post('/auth/register_local')
        .send({id:"dumb", password:"abcdefghijklmnopqrst1"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('Password only digits', function(done) {
      request.post('/auth/register_local')
        .send({id:"numb", password:"111111"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('Password only letters', function(done) {
      request.post('/auth/register_local')
        .send({id:"numbnumb", password:"abcdef"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('Password with whitespace', function(done) {
      request.post('/auth/register_local')
        .send({id:"hacker", password:"sql injection"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });
  });

  describe('Facebook Function', function() {
    var token;
    var token2;
    var fb_token = "correct";
    var fb_token2 = "correct2";
    before(function(done) {
      request.post('/auth/login_local')
      .send({id:"snutt", password:"abc1234"})
      .expect(200)
      .end(function(err, res){
        token = res.body.token;
        done(err);
      });
    });

    before(function(done) {
      request.post('/auth/login_local')
      .send({id:"snutt2", password:"abc1234*"})
      .expect(200)
      .end(function(err, res){
        token2 = res.body.token;
        done(err);
      });
    });

    it('Log-in with facebook fails when no fb_id', function(done) {
      request.post('/auth/login_fb')
        .expect(400)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.NO_FB_ID_OR_TOKEN);
          done(err);
        });
    });

    it('Attach fails when no fb_id', function(done) {
      request.post('/user/facebook')
        .set('x-access-token', token)
        .expect(400)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.NO_FB_ID_OR_TOKEN);
          done(err);
        });
    });

    it('Attach Facebook ID', function(done) {
      let fbId = "1234";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token).resolves({
        fbName: "John",
        fbId: "1234"
      });

      request.post('/user/facebook')
        .set('x-access-token', token)
        .send({fb_id: fbId, fb_token: fb_token})
        .expect(200)
        .end(function(err, res){
          token = res.body.token;
          done(err);
        });
    });

    it('Attach fails when already attached', function(done) {
      let fbId = "1234";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token).resolves({
        fbName: "John",
        fbId: "1234"
      });

      request.post('/user/facebook')
        .set('x-access-token', token)
        .send({fb_id: fbId, fb_token: fb_token})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.ALREADY_FB_ACCOUNT);
          done(err);
        });
    });

    it('Attach fails when already attached fb_id', function(done) {
      let fbId = "1234";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token).resolves({
        fbName: "John",
        fbId: "1234"
      });

      request.post('/user/facebook')
        .set('x-access-token', token2)
        .send({fb_id: fbId, fb_token: fb_token})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.FB_ID_WITH_SOMEONE_ELSE);
          done(err);
        });
    });

    it('Facebook status holds true', function(done) {
      request.get('/user/facebook')
        .set('x-access-token', token)
        .expect(200)
        .end(function(err, res){
          assert.equal(res.body.name, "John");
          assert.equal(res.body.attached, true);
          done(err);
        });
    });

    it('Log-in with facebook succeeds', function(done) {
      let fbId = "1234";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token).resolves({
        fbName: "John",
        fbId: "1234"
      });

      request.post('/auth/login_fb')
        .send({fb_id: fbId, fb_token: fb_token})
        .expect(200)
        .end(function(err, res){
          assert.equal(res.body.token, token);
          done(err);
        });
    });

    it('Detach Facebook ID', function(done) {
      request.delete('/user/facebook')
        .set('x-access-token', token)
        .expect(200)
        .end(function(err, res){
          token = res.body.token;
          done(err);
        });
    });

    it('Facebook status holds false', function(done) {
      request.get('/user/facebook')
        .set('x-access-token', token)
        .expect(200)
        .end(function(err, res){
          assert.equal(res.body.attached, false);
          done(err);
        });
    });

    it('Detach fails when already detached', function(done) {
      request.delete('/user/facebook')
        .set('x-access-token', token)
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.NOT_FB_ACCOUNT);
          done(err);
        });
    });

    it('Auto-register when log-in with not attached fb_id', function(done){
      let fbId = "12345";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token2).resolves({
        fbName: "Smith",
        fbId: fbId
      });

      request.post('/auth/login_fb')
        .send({fb_id: fbId, fb_token: fb_token2})
        .expect(200)
        .end(function(err, res){
          token = res.body.token;
          done(err);
        });
    });

    it('Auto-generated default timetable', function(done) {
      request.get('/tables/')
        .set('x-access-token', token)
        .expect(200)
        .end(function(err, res) {
          if (err) done(err);
          assert.equal(res.body[0].title, "나의 시간표");
          assert.equal(res.body[0].year, 2016);
          assert.equal(res.body[0].semester, 3);
          done();
        });
    });

    it('Accounts with only facebook credential cannot detach FB ID', function(done){
      request.delete('/user/facebook')
        .set('x-access-token', token)
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.NOT_LOCAL_ACCOUNT);
          done(err);
        });
    });

    it('Log-in fails with incorrect access token', function(done){
      let fbId = "12345";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token2).resolves({
        fbName: "Smith",
        fbId: fbId
      });

      request.post('/auth/login_fb')
        .send({fb_id: fbId, fb_token: "incorrect"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_FB_TOKEN);
          done(err);
        });
    });

    it('Log-in fails with incorrect fb_id', function(done){
      let fbId = "123456";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token2).rejects(new InvalidFbIdOrTokenError(fbId, fb_token2));

      request.post('/auth/login_fb')
        .send({fb_id: fbId, fb_token: fb_token2})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_FB_TOKEN);
          done(err);
        });
    });

    it('attach local id with duplicate id fails', function(done){
      request.post('/user/password')
        .set('x-access-token', token)
        .send({id: "snutt", password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.DUPLICATE_ID);
          done(err);
        });
    });

    it('attach local id without id fails', function(done){
      request.post('/user/password')
        .set('x-access-token', token)
        .send({password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_ID);
          done(err);
        });
    });

    it('attach local id without password fails', function(done){
      request.post('/user/password')
        .set('x-access-token', token)
        .send({id:"snuttfb"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.INVALID_PASSWORD);
          done(err);
        });
    });

    it('attach local id', function(done){
      request.post('/user/password')
        .set('x-access-token', token)
        .send({id:"snuttfb", password:"abc1234"})
        .expect(200)
        .end(function(err, res){
          token = res.body.token;
          done(err);
        });
    });

    it('log-in with password succeeds', function(done) {
      request.post('/auth/login_local')
        .send({id:"snuttfb", password:"abc1234"})
        .expect(200)
        .end(function(err, res){
          assert.equal(token, res.body.token);
          done(err);
        });
    });

    it('log-in with facebook still succeeds', function(done){
      let fbId = "12345";
      let facebookGetFbInfoStub = sinonSandbox.stub(FacebookService, 'getFbInfo');
      facebookGetFbInfoStub.withArgs(fbId, fb_token2).resolves({
        fbName: "Smith",
        fbId: fbId
      });

      request.post('/auth/login_fb')
        .send({fb_id:"12345", fb_token: fb_token2})
        .expect(200)
        .end(function(err, res){
          assert.equal(token, res.body.token);
          done(err);
        });
    });

    it('attach local id again fails', function(done){
      request.post('/user/password')
        .set('x-access-token', token)
        .send({id:"snuttfb", password:"abc1234"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.ALREADY_LOCAL_ACCOUNT);
          done(err);
        });
    });
  });

  describe('Account Removal', function() {
    var token;
    before(function(done) {
      request.post('/auth/register_local')
        .send({id:"snuttar", password:"abc1234*"})
        .expect(200)
        .end(function(err, res){
          assert.equal(res.body.message, 'ok');
          done(err);
        });
    });

    before(function(done) {
      request.post('/auth/login_local')
        .send({id:"snuttar", password:"abc1234*"})
        .expect(200)
        .end(function(err, res){
          token = res.body.token;
          done(err);
        });
    });

    it('account remove succeed', function(done) {
      request.delete('/user/account')
        .set('x-access-token', token)
        .expect(200)
        .end(function(err, res){
          assert.equal(res.body.message, 'ok');
          done(err);
        });
    });

    it('token transaction fails with removed account', function(done) {
      request.get('/user/info')
        .set('x-access-token', token)
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_USER_TOKEN);
          done(err);
        });
    });

    it('log-in with removed account fails', function(done) {
      request.post('/auth/login_local')
        .send({id:"snuttar", password:"abc1234*"})
        .expect(403)
        .end(function(err, res){
          assert.equal(res.body.errcode, ErrorCode.WRONG_ID);
          done(err);
        });
    });

    it('re-register with same id succeeds', function(done) {
      request.post('/auth/register_local')
        .send({id:"snuttar", password:"abc1234*"})
        .expect(200)
        .end(function(err, res){
          assert.equal(res.body.message, 'ok');
          done(err);
        });
    });
  });


  it('Request temporary account', function(done) {
    request.post('/auth/request_temp')
      .expect(200)
      .end(function(err, res) {
        token_temp = res.body.token;
        done(err);
      });
  });

  it('Request temporary account again', function(done) {
    request.post('/auth/request_temp')
      .expect(200)
      .end(function(err, res) {
        assert.notEqual(res.body.token, token_temp);
        token_temp = res.body.token;
        done(err);
      });
  });

  it ('Temporary account works OK', function(done){
    request.get('/tables/')
      .set('x-access-token', token_temp)
      .expect(200)
      .end(function(err, res) {
        if (err) done(err);
        assert.equal(res.body.length, 1);
        done();
      });
  });

  it('attach local id into temp account', function(done){
    request.post('/user/password')
      .set('x-access-token', token_temp)
      .send({id:"snutttemp", password:"abc1234"})
      .expect(200)
      .end(function(err, res){
        token_temp = res.body.token;
        done(err);
      });
  });

  it('Log-in succeeds after attaching local_id into temp account', function(done) {
    request.post('/auth/login_local')
      .send({id:"snutttemp", password:"abc1234"})
      .expect(200)
      .end(function(err, res){
        assert.equal(res.body.token, token_temp);
        done(err);
      });
  });
};
