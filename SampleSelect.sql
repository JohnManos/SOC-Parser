SELECT GROUP_CONCAT(DISTINCT "Id" ORDER BY 1 SEPARATOR ' ') "Id", 
		GROUP_CONCAT(DISTINCT "Course" ORDER BY 1 SEPARATOR ' ') "Course", 
		GROUP_CONCAT("Day/s" ORDER BY 1 SEPARATOR ' ') "Day/s", 
		GROUP_CONCAT("Time" ORDER BY 1 SEPARATOR ' ') "Time", 
        "Sect", 
        "Class Nbr" 
FROM SAMPLE 
GROUP BY "Class Nbr", "Sect"