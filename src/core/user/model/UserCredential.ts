export default interface UserCredential {
  localId?: string;
  localPw?: string;
  fbName?: string;
  fbId?: string;
  appleEmail?: string;
  appleSub?: string;
  appleTransferSub?: string;
  tempDate?: Date;
  tempSeed?: number;
};
