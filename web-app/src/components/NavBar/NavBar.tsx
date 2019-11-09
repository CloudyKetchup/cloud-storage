import React, {FunctionComponent} from 'react'

import {NavFolder} 		from './NavFolder/NavFolder';
import NavNode 			from '../../model/NavNode';
import {EntityHelpers} from "../../helpers";

type Props = {
	navNodes: NavNode[]
};

const NavBar: FunctionComponent<Props> = props => (

	<nav>
		<div className="navigation-folders">
			{props.navNodes.map(node => <NavFolder key={EntityHelpers.uuidv4()} data={node}/>)}
		</div>
	</nav>
);

export default NavBar;