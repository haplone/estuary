package com.neighborhood.aka.laplace.estuary.mysql.schema.defs.ddl;

import com.neighborhood.aka.laplace.estuary.mysql.schema.defs.columndef.ColumnDef;

public class AddColumnMod extends ColumnMod {
	public ColumnDef definition;
	public ColumnPosition position;

	public AddColumnMod(String name, ColumnDef d, ColumnPosition position) {
		super(name);
		this.definition = d;
		this.position = position;
	}

}

