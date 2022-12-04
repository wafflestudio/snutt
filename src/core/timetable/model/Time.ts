export interface Time {
  subtract(other: Time): Time;

  addMinute(minute: number);

  addHour(hour: number);

  subtractMinute(minute: number);

  subtractHour(hour: number);

  toHourMinuteFormat();

  getHour(): number;

  getMinute(): number;

  getDecimalHour(): number;
}

export class Time implements Time {
  minute: number;

  constructor(minute: number) {
    this.minute = minute
  }

  static fromHourMinuteString(hourMinuteString: string) {
    const hourMinute = hourMinuteString.split(":")
    if (hourMinute.length !== 2) throw new HourMinuteFormatException()
    const minute = Number(hourMinute[0]) * 60 + Number(hourMinute[1])
    return new Time(minute);
  }

  subtract(other: Time): Time {
    return new Time(this.minute - other.minute)
  }

  addMinute(minute: number) {
    return new Time(this.minute + minute)
  }

  addHour(hour: number) {
    return new Time(this.minute + hour * 60)
  }

  subtractMinute(minute: number) {
    return new Time(this.minute - minute)
  }

  subtractHour(hour: number) {
    return new Time(this.minute - hour * 60)
  }

  toHourMinuteFormat() {
    return `${String(this.getHour()).padStart(2, '0')}:${String(this.getMinute()).padStart(2, '0')}`
  }

  getHour(): number {
    return Math.trunc(this.minute / 60)
  }

  getMinute(): number {
    return Math.trunc(this.minute % 60)
  }

  getDecimalHour(): number {
    return this.minute / 60
  }

}

export class HourMinuteFormatException {
}
