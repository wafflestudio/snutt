export default interface RedisVerificationValue {
  email: string,
  code: string,
  count: number, 
  createdAtTimestamp: number
}
