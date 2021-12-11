export default interface AbstractTimetable {
    _id: string,
    year: number;
    semester: number;
    title: string;
    total_credit: number;
    updated_at: Date;
};
