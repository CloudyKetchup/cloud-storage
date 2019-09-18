export default class NavNode {
	
	id: string;
	name: string;
	onClick: () => void;
	prev: NavNode | undefined;
	next: NavNode | null = null;

    constructor(id: string, name: string, prev: NavNode | undefined, onClick: () => void) {
		this.id 	= id;
		this.name	= name;
		this.onClick	= onClick;
		this.prev	= prev;
	}
}
