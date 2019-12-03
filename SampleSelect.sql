SELECT GROUP_CONCAT(DISTINCT "Id" ORDER BY "Id" SEPARATOR ' ') "Id", 
		GROUP_CONCAT(DISTINCT "Course" ORDER BY "Id" SEPARATOR ' ') "Course", 
		GROUP_CONCAT("Day/s" ORDER BY "Id" SEPARATOR ' ') "Day/s", 
        "Sect", 
        "Class Nbr" 
FROM SAMPLE 
GROUP BY "Class Nbr", "Sect"