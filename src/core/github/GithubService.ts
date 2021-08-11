import request = require('request-promise-native');
import property = require('@app/core/config/property');
import GithubIssue from './model/GithubIssue';

let githubToken = property.get('core.feedback2github.token');
let apiHeader = {
    Accept: "application/vnd.github.v3+json",
    Authorization: "token " + githubToken,
    "User-Agent": "feedback2github"
};

export async function getUserName(): Promise<string> {
    let result = await request({
        method: 'GET',
        uri: "https://api.github.com/user",
        headers: apiHeader,
        json: true
    })

    return result.login;
}

export async function addIssue(repoOwner: string, repoName: string, issue: GithubIssue): Promise<string> {
    let apiIssuesUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/issues";
    let response = await request({
        method: 'POST',
        uri: apiIssuesUrl,
        headers: apiHeader,
        body: issue,
        json: true
    });

    return response;
}
