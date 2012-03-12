#!/usr/bin/env python

import ConfigParser
import getopt
import sys
import xmlrpclib
import subprocess, threading

config = ConfigParser.ConfigParser()

NODE_FILTERS = [{'hostname':'*pjwstk*'},{'hostname':'*.pl'},{}]
IGNORED_NODES = [14625,14626,14635,14629,14577,14634,831,1174,14630,14543,832,640,14304,14305,14306,14307,14321,14340,14344,13439,13440,320,377,621,1145,14636,378,14362,14425,1146,7023,744,353,14391,357,737,1164,14763,14257,7035,14365,14722,14541,930,931,14536,987,14530,14972,14957,]


class PLHelper():
    
    def __init__(self, username, password, nodes_count):
        self.username = username
        self.password = password
        self.nodes_count = nodes_count
    
    def auth(self):
        self.api_server = xmlrpclib.ServerProxy('https://www.planet-lab.eu/PLCAPI/', allow_none=True)
        self.auth = {
            'AuthMethod': 'password',
            'Username': self.username,
            'AuthString': self.password
        }
        return self.api_server.AuthCheck(self.auth)
    
    def get_nodes(self, count, node_filter={}, ignore_ids=[]):
        print "Getting %s nodes filter=%s" % (count,node_filter)
        node_filter['-LIMIT'] = count
        node_filter['boot_state'] = 'boot'
        node_filter['run_level'] = 'boot'
        node_filter['-SORT'] = 'last_contact'
        nodes = []
        all_nodes = self.api_server.GetNodes(self.auth, node_filter, ['node_id','boot_state','run_level','hostname','last_contact'])
        i = 0
        for node in all_nodes:
            if node['node_id'] in ignore_ids: continue
            if i < count:
                nodes.append(node)
            i += 1
        return nodes
    
    def get_slices_nodes(self, slice_ids=[]):
        print "Getting nodes for slices %s" % slice_ids
        return self.api_server.GetNodes(self.auth, {'|slice_ids': [slice_ids,]})
    
    def add_slice_to_nodes(self, slice_id, node_ids=[]):
        print "Adding slice %s to nodes: %s" % (slice_id,node_ids)
        return self.api_server.AddSliceToNodes(self.auth, slice_id, node_ids)
    
    def delete_slice_from_nodes(self, slice_id, node_ids=[]):
        print "Deleting slice %s to nodes: %s" % (slice_id,node_ids)
        return self.api_server.DeleteSliceFromNodes(self.auth, slice_id, node_ids)


class Command(object):
    class Callable:
        def __init__(self,fname):
            self.__call__ = fname
    
    def __init__(self, cmd):
        self.cmd = cmd
        self.process = None

    def run(self, timeout):
        def target():
            print "Executing command with timeout=%s: %s" % (timeout,self.cmd)
            self.process = subprocess.Popen(self.cmd, shell=True)
            self.process.communicate()

        thread = threading.Thread(target=target)
        thread.start()

        if timeout > 0:
            thread.join(timeout)
            if thread.is_alive():
                self.process.terminate()
                thread.join()
            return self.process.returncode
        else:
            return 0

    def run_command(cmd, timeout):
        if not type(cmd) == type(""): cmd = str(cmd)
        command = Command(cmd)
        code = command.run(timeout)
        if code != 0:
            print "Error while executing command \"%s\"" % cmd
        return code
    run_command = Callable(run_command)


def test(opts={}):
    
    pl_helper = PLHelper(config.get('credentials', 'username'), config.get('credentials', 'password'), config.getint('api', 'nodes_count'))
    pl_helper.auth()
    
    nodes = {}
    node_filters_len = len(NODE_FILTERS)
    
    for i in range(0,len(NODE_FILTERS)):
        NODE_FILTERS[i]['~|slice_ids'] = [config.getint('api', 'slice_id'),]
        NODE_FILTERS[i]['~node_id'] = IGNORED_NODES
    
    node_filters_i = 0
    while len(nodes) < config.getint('api', 'nodes_count') and node_filters_i < node_filters_len:
        nodes_tmp = pl_helper.get_nodes(config.getint('api', 'nodes_count')-len(nodes), NODE_FILTERS[node_filters_i], nodes.keys()); node_filters_i+=1
        for node in nodes_tmp:
            if not node['node_id'] in nodes:
                nodes[node['node_id']] = node
        
    i = 1
    for node_id,node in nodes.iteritems():
        print "%s) %s: %s" % (i,node_id, node['hostname'])
        i += 1
        
    i = 1
    for node in pl_helper.get_slices_nodes([config.getint('api', 'slice_id'),]):
        print "%s) %s: %s" % (i,node['node_id'], node['hostname'])
        i += 1


def deploy_nodes(opts={}):
    
    pl_helper = PLHelper(config.get('credentials', 'username'), config.get('credentials', 'password'), config.getint('api', 'nodes_count'))
    pl_helper.auth()
    
    node_ids = []
    node_filters_len = len(NODE_FILTERS)
    
    for i in range(0,len(NODE_FILTERS)):
        NODE_FILTERS[i]['~|slice_ids'] = [config.getint('api', 'slice_id'),]
        NODE_FILTERS[i]['~node_id'] = IGNORED_NODES
    
    node_filters_i = 0
    while len(node_ids) < config.getint('api', 'nodes_count') and node_filters_i < node_filters_len:
        nodes = pl_helper.get_nodes(config.getint('api', 'nodes_count')-len(node_ids), NODE_FILTERS[node_filters_i], node_ids); node_filters_i+=1
        for node in nodes:
            node_ids.append(node['node_id'])
        
    print pl_helper.add_slice_to_nodes(config.getint('api', 'slice_id'), node_ids)
    

def remove_nodes(opts={}):
    
    pl_helper = PLHelper(config.get('credentials', 'username'), config.get('credentials', 'password'), config.getint('api', 'nodes_count'))
    pl_helper.auth()
    
    nodes = pl_helper.get_slices_nodes([config.getint('api', 'slice_id'),])
    node_ids = [node['node_id'] for node in nodes]
    
    print pl_helper.delete_slice_from_nodes(config.getint('api', 'slice_id'), node_ids)
    
    
def execute_command(opts={}):
    
    if opts is None or type(opts) != type({}):
        print 'Execute command mode arguments must be a dictionary'; return
    if 'command' not in opts:
        print 'Execute command mode arguments must contain command entry'; return
    if 'ssh' in opts and not 'ssh_user' in opts:
        print 'Execute command mode requires ssh_user along with ssh'; return
    
    command = opts['command']
    timeout = opts['timeout'] if 'timeout' in opts else 0
    through_ssh = 'ssh' in opts and opts['ssh']
    ssh_user = opts['ssh_user'] if 'ssh_user' in opts else None
    copy_id = 'copy_id' in opts and opts['copy_id']
    
    node_id = None
    if 'node_id' in opts:
        if type(opts['node_id']) != type(0) and type(opts['node_id']) != type([]) or \
            (type(opts['node_id']) == type([]) and (len(opts['node_id']) == 0 or type(opts['node_id'][0]) != type(0))):
            print 'Execute command mode argument node_id must be a int or an array of ints'
        node_id = opts['node_id'] if type(opts['node_id']) == type([]) else [opts['node_id'],]
    
    pl_helper = PLHelper(config.get('credentials', 'username'), config.get('credentials', 'password'), config.getint('api', 'nodes_count'))
    pl_helper.auth()
    
    if node_id is None:
        nodes = pl_helper.get_slices_nodes([int(config.get('api', 'slice_id')),])
    else:
        nodes = pl_helper.get_nodes(len(node_id), {'node_id':node_id})
    
    hostnames = [node['hostname'] for node in nodes]
    
    for i,hostname in enumerate(hostnames):
        code = 0
        if through_ssh: # -m execute -a "{'command':'\"touch %s  > /dev/null 2>&1 &\"%i', 'timeout':20, 'ssh':True, 'ssh_user':'pjwstkple_p2pp', 'copy_id':True}"
            if copy_id:
                code = Command.run_command("./ssh-copy-id.sh -i ~/.ssh/id_planetlab %s@%s" % (ssh_user,hostname), timeout)
            if code == 0:
                code = Command.run_command("./sshcmd.sh -c \"%s\" -u %s -h %s" % (eval(command),ssh_user,hostname), timeout)
        else: # -m execute -a "{'command':'\"echo %s\"%hostname', 'timeout':10}"
            code = Command.run_command(eval(command), timeout) 


def main(argv=None):
    if argv is None:
        argv = sys.argv
    
    try:
        opts, args = getopt.getopt(argv, 'u:p:s:m:a:c:', ['username=','password=','slice=','mode=','mode_args=','count=',])
    except getopt.GetoptError:     
        sys.exit(2)
    opts = dict(opts)
    
    MODE = None
    MODE_ARGS = None
    config.read('pldep.cfg')
    
    for o,v in opts.items():
        if o == '-u' or o == '--username': config.set('credentials', 'username', v)
        if o == '-p' or o == '--password': config.set('credentials', 'password', v)
        if o == '-s' or o == '--slice': config.set('api', 'slice_id', v)
        if o == '-m' or o == '--mode': MODE = v
        if o == '-a' or o == '--mode_args': MODE_ARGS = v
        if o == '-c' or o == '--count': config.set('api', 'nodes_count', v)
        
    if not config.has_option('credentials', 'username') or \
        not config.has_option('credentials', 'password') or \
        not config.has_option('api', 'slice_id') or MODE is None:
        print "Username, password, slice and mode must passed in arguments"
        sys.exit(2)
    
    mode_functions = {
        'test': test,
        'deploy': deploy_nodes,
        'remove': remove_nodes,
        'execute': execute_command,
    }
    
    if MODE in mode_functions:
        mode_args = MODE_ARGS
        try:
            mode_args = eval(MODE_ARGS)
        except:
            mode_args = "\"%s\"" % MODE_ARGS
        mode_functions[MODE](eval(MODE_ARGS) if not MODE_ARGS is None else {})
    else:
        print "Unsupported mode passed: %s" % MODE
        sys.exit(2)


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv[1:]))
    except KeyboardInterrupt:
        print "Quitting: CTRL-C pressed"
    except SystemExit,e:
        if not e.code is None:
            print "Quitting: %s" % e
