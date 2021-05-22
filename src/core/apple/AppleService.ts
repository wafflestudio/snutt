/**
 * 받은 identityToken(JWT) header를 분석, kid와 alg에 해당하는 JWK를 찾아 토큰 검증, 해독
 *
 * @author Hank Choi, zlzlqlzl1@gmail.com
 */

import * as jwt from "jsonwebtoken";
import AppleUserInfo from "@app/core/apple/model/AppleUserInfo";
import AppleJWK from "@app/core/apple/model/AppleJWK";
import request = require("request");
import AppleApiError from "@app/core/apple/error/AppleApiError";
import pemjwk = require("pem-jwk");
import JwtHeader from "@app/core/apple/model/JwtHeader";
import InvalidAppleTokenError from "@app/core/apple/error/InvalidAppleTokenError";

async function getMatchedKeyBy(kid: string, alg: string): Promise<AppleJWK> {
    try {
        const keys: Array<AppleJWK> = await new Promise<Array<AppleJWK>>(function (resolve, reject) {
            request({
                url: "https://appleid.apple.com/auth/keys",
                method: "GET",
                json: true,
            }, function (err, res, body) {
                if (err || res.statusCode != 200 || !body) {
                    return reject(new AppleApiError());
                } else {
                    return resolve(body.keys);
                }
            });
        });
        return keys.filter((key) => key.kid === kid && key.alg === alg)[0]
    } catch (err) {
        throw err
    }
}

export async function verifyAndDecodeAppleToken(identityToken: string): Promise<AppleUserInfo> {
    const headerOfIdentityToken: JwtHeader = JSON.parse(Buffer.from(identityToken.substr(0, identityToken.indexOf('.')), 'base64').toString());
    const appleJwk: AppleJWK = await getMatchedKeyBy(headerOfIdentityToken.kid, headerOfIdentityToken.alg);
    const publicKey: string = pemjwk.jwk2pem(appleJwk);
    try {
        jwt.verify(identityToken, publicKey, {
            algorithms: [appleJwk.alg],
            issuer: "https://appleid.apple.com",
            audience: "com.wafflestudio.snutt"
        });
    }
    catch (err) {
        throw new InvalidAppleTokenError(identityToken);
    }
    return <AppleUserInfo>jwt.decode(identityToken)
}
