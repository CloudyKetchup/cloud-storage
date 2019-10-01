import React, {FunctionComponent} from 'react';
import NavNode from '../../../model/NavNode';

export const NavFolder: FunctionComponent<{data: NavNode}> = props => (

	<div className="nav-folder" onClick={props.data.onClick}>
		<div className="nav-folder-name">
			{props.data.name}
		</div>
		<div className="nav-folder-arrow">
			<i className="fas fa-chevron-right"/>
		</div>
	</div>
);
