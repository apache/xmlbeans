for $a in $this//employee
return <result>
		{ $a/ssn },
		{ $a/name },
		{
		for $b in $this//employee 
		where $b/ssn=$a/ssn and $a/name !=$b/name
		return $b/name
		}
	</result>
