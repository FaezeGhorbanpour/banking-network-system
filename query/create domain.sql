create domain Dname as varchar(20) 
check (value notnull );

create domain Dusername as varchar(30) 
check (value notnull );

create domain pass as varchar(30) 
check (value notnull );

create domain Dtoken as varchar(7) 
check (value notnull );