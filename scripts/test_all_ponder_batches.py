#!/usr/bin/env python3
"""Render every job from one successful Ponder scan using identical JAR inputs."""
import argparse,csv,datetime,json,subprocess,sys
from pathlib import Path
from build import ROOT
from test_three_addons import owned,write_json
from test_all_ponder_client import classify_internal_errors

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--scan',type=Path,required=True)
    parser.add_argument('--candidates',type=Path,required=True)
    parser.add_argument('--batch-size',type=int,default=40)
    parser.add_argument('--resume-report',type=Path,action='append',default=[])
    args=parser.parse_args()
    if not 1<=args.batch_size<=100:raise ValueError('Batch size must be1..100')
    scan=owned(args.scan);data=json.loads(scan.read_text(encoding='utf-8'))
    if not data.get('passed') or data.get('internal_errors')!=[]:raise ValueError('Need successful strict scan')
    candidates=args.candidates.resolve()
    if not (candidates.is_relative_to((ROOT/'runtime').resolve()) or candidates.is_relative_to((ROOT/'.local').resolve())):raise ValueError('Candidate manifest must stay within project local/runtime')
    jars=json.loads(candidates.read_text(encoding='utf-8'))['jars']
    command=[sys.executable,'-X','utf8','-B',str(ROOT/'scripts/test_all_ponder_client.py'),'render','--scan',str(scan),'--storage',jars['storage-adapted.jar']]
    for name,path in jars.items():
        if name!='storage-adapted.jar':command+=['--candidate-mod',path]
    run=owned(ROOT/'runtime'/('ponder-batches-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')));run.mkdir(parents=True)
    result={'scan':str(scan),'candidate_jars':jars,'jar_sha256':data['jar_sha256'],'requested_jobs':len(data['probe']['render_jobs']),'batches':[],'passed':False,'original_saves_accessed':False}
    output=run/'batches.json'
    def save():write_json(output,result);write_json(ROOT/'.local/last-ponder-batches.json',{'manifest':str(output)})
    save();all_rows=[];prefix=0
    for original in args.resume_report:
        original=owned(original);row=json.loads(original.read_text(encoding='utf-8'));probe=row.get('probe',{});rendered=probe.get('rendered',[])
        assert row['start']==prefix and row['exit_code']==0 and not row['timed_out'] and probe.get('passed') is True and probe.get('saved_and_returned_to_title') is True
        assert row['jar_sha256']==data['jar_sha256']
        expected=data['probe']['render_jobs'][prefix:prefix+row['limit']]
        actual=[{k:v for k,v in item.items() if k in expected[0]} for item in rendered]
        assert actual==expected and all(r.get('finite_instruction_schedule_complete') is True for r in rendered)
        errors=classify_internal_errors(owned(Path(row['log'])).read_text(encoding='utf-8',errors='replace'));assert errors==[],errors
        reassessed=run/f'reassessed-{prefix:04}.json';derived=dict(row);derived.update(passed=True,internal_errors=[],source_report=str(original),log_reassessment={'original_passed':row['passed'],'original_internal_errors':row['internal_errors'],'reason':'Exact cosmetic-network message excluded after source/stack review; original raw report retained; game execution unchanged'})
        write_json(reassessed,derived);result['batches'].append({'start':prefix,'expected':len(expected),'completed':len(rendered),'passed':True,'report':str(reassessed),'internal_errors':[],'failure':None,'resumed_from':str(original)});all_rows.extend(rendered);prefix+=len(rendered);save()
    for start in range(prefix,result['requested_jobs'],args.batch_size):
        with (run/f'batch-{start:04}.log').open('wb') as log:
            child=subprocess.run([*command,'--start',str(start),'--limit',str(args.batch_size)],cwd=ROOT,stdout=log,stderr=subprocess.STDOUT)
        pointer=json.loads((ROOT/'.local/last-all-ponder-render.json').read_text(encoding='utf-8'));report=owned(Path(pointer['result']));row=json.loads(report.read_text(encoding='utf-8'));probe=row.get('probe',{});rendered=probe.get('rendered',[])
        expected=data['probe']['render_jobs'][start:start+args.batch_size]
        actual=[{k:v for k,v in item.items() if k in expected[0]} for item in rendered]
        passed=(child.returncode==0 and row.get('passed') is True and row.get('internal_errors')==[] and row['jar_sha256']==data['jar_sha256'] and actual==expected and all(r.get('finite_instruction_schedule_complete') is True for r in rendered) and probe.get('saved_and_returned_to_title') is True)
        item={'start':start,'expected':len(expected),'completed':len(rendered),'passed':passed,'report':str(report),'internal_errors':row.get('internal_errors'),'failure':probe.get('error')};result['batches'].append(item);all_rows.extend(rendered);save()
        print(json.dumps(item,ensure_ascii=False),flush=True)
        if not passed:return 1
    result['passed']=len(all_rows)==result['requested_jobs'];result['completed_jobs']=len(all_rows);result['rendered_gui_frames']=sum(r['rendered_gui_frames'] for r in all_rows);result['timeline_samples']=sum(r['timeline_samples'] for r in all_rows)
    with (run/'rendered-scenes.csv').open('w',encoding='utf-8',newline='') as stream:
        writer=csv.DictWriter(stream,fieldnames=list(all_rows[0]));writer.writeheader();writer.writerows(all_rows)
    save();print(json.dumps({k:v for k,v in result.items() if k not in ['candidate_jars','jar_sha256','batches']},ensure_ascii=False),flush=True)
    return 0 if result['passed'] else 1
if __name__=='__main__':raise SystemExit(main())
