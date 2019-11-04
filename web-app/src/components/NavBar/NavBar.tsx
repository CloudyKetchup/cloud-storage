import React, {FunctionComponent} from 'react'

import {NavFolder} 		from './NavFolder/NavFolder';
import NavNode 			from '../../model/NavNode';

type Props = {
	navNodes: NavNode[]
};

const NavBar: FunctionComponent<Props> = props => (

	<nav>
		<div className="navigation-folders">
			{props.navNodes.map(node => <NavFolder key={node.id} data={node}/>)}
		</div>
	</nav>
);

export default NavBar;