// SharedReview+Date.swift
import Foundation
import Shared

// פרסור אוניברסלי ל-createdAt (ms/seconds/ISO/cust)
private enum _DateParse {
    static let isoFrac: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return f
    }()
    static let iso: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime]
        return f
    }()

    static func df(_ fmt: String) -> DateFormatter {
        let df = DateFormatter()
        df.locale = Locale(identifier: "en_US_POSIX")
        df.timeZone = TimeZone(secondsFromGMT: 0)
        df.dateFormat = fmt
        return df
    }

    static let customFormatters: [DateFormatter] = [
        df("yyyy-MM-dd HH:mm:ss"),
        df("yyyy-MM-dd'T'HH:mm:ss.SSS"),
        df("yyyy-MM-dd'T'HH:mm:ss"),
        df("yyyy/MM/dd HH:mm:ss"),
        df("dd/MM/yyyy HH:mm")
    ]

    static func parse(_ s: String) -> Date? {
        let t = s.trimmingCharacters(in: .whitespacesAndNewlines)
        // מספר? (ms/seconds)
        if let i = Int64(t) {
            // 10^10 ~ שניות; מעל זה כנראה מילישניות
            let isMillis = i > 10_000_000_000
            return Date(timeIntervalSince1970: TimeInterval(isMillis ? Double(i) / 1000.0 : Double(i)))
        }
        if let d = Double(t), d < 10_000_000_000 {
            return Date(timeIntervalSince1970: d) // seconds as double
        }
        // ISO8601
        if let d1 = isoFrac.date(from: t) { return d1 }
        if let d2 = iso.date(from: t) { return d2 }
        // Custom patterns
        for f in customFormatters {
            if let d = f.date(from: t) { return d }
        }
        return nil
    }
}

public extension Shared.Review {
    /// תאריך יצירה "חכם" – תומך במספרים/ISO/פורמטים נפוצים.
    var createdAtDateSafe: Date {
        _DateParse.parse(createdAt) ?? Date() // נפילה קדימה: עכשיו
    }

    /// מילי־שניות נוחות להשוואה/מיון
    var createdAtMillisSafe: Int64 {
        Int64(createdAtDateSafe.timeIntervalSince1970 * 1000.0)
    }

    var ratingInt: Int { Int(rating ?? 0) }
    var addressSafe: String { address ?? "" }
}
